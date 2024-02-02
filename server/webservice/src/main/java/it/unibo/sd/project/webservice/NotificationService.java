package it.unibo.sd.project.webservice;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import it.unibo.sd.project.webservice.rabbit.MessageType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class NotificationService extends AbstractVerticle {
    private final Map<String, Set<String>> matchPlayersMap, playersSocketMap;
    private final Map<String, String> playersStatusMap;
    public static final String WS_EVENTS_ADDRESS = WebServer.BASE_ADDRESS + "notification.events.";
    public static final String WS_PLAYER_REGISTRATION = WebServer.BASE_ADDRESS + "notification.player-registration.";
    public static final String WS_PLAYER_STATUS = WebServer.BASE_ADDRESS + "notification.player-status.";
    public static final String WS_PLAYER_DISCONNECTION = WebServer.BASE_ADDRESS + "notification.player-disconnection.";
    private static final String ONLINE = "online", OFFLINE = "offline";

    public NotificationService() {
        matchPlayersMap = new HashMap<>();
        playersStatusMap = new HashMap<>();
        playersSocketMap = new HashMap<>();
    }

    @Override
    public void start() {
        /* HANDLER FOR MESSAGES SENT FROM WebServer TO NotificationService */
        // Handler executed each time the server needs to send a
        // notification to clients related to game events
        vertx.eventBus().consumer(WS_EVENTS_ADDRESS, message -> {
            JsonObject notificationRequest = new JsonObject(message.body().toString());
            MessageType notificationType = MessageType.valueOf(notificationRequest.getString("notificationType"));
            JsonObject data = notificationRequest.getJsonObject("data");
            switch (notificationType) {
                case CREATE_ROOM:
                    createCommunicationRoom(data.getJsonArray("matchesArray"));
                    break;
                case NEW_MATCH:
                case NEW_MOVE:
                case MATCH_OVER:
                    String matchID = data.getString("matchID");
                    String originPlayer = data.getString("originPlayer");
                    JsonObject notificationData = new JsonObject()
                            .put("notificationType", notificationType.getType())
                            .put("originPlayer", originPlayer);
                    notify(matchID, originPlayer, notificationData);
                    break;
                default:
                    System.out.println("Invalid notification type");
            }
        });

        /* ++++++++++++++++++++++++++++++++++++++++++++++++++++++ */
        /* ++++++++++++++++++++++++++++++++++++++++++++++++++++++ */

        /* HANDLERS FOR MESSAGES SENT FROM CLIENTS TO SERVER */
        // Handler executed each time a clients logs in and wants to be detected by
        // the server and, hence, by other players
        vertx.eventBus().consumer(WS_PLAYER_REGISTRATION, message -> {
            JsonObject playerRegistrationRequest = new JsonObject(message.body().toString());
            String playerToRegister = playerRegistrationRequest.getString("username");
            System.out.println("Registering " + playerToRegister);

            String socketAddress = playerRegistrationRequest.getString("socketAddress");
            playersSocketMap.computeIfAbsent(playerToRegister, k -> new HashSet<>()).add(socketAddress);

            // first of all reply to this message with the current state of all registered players
            vertx.executeBlocking(() -> {
                JsonArray allPlayersState = new JsonArray();
                playersStatusMap.forEach((player, status) -> {
                    if (!player.equals(playerToRegister)) {
                        JsonObject playerState = new JsonObject()
                                .put("username", player)
                                .put("status", status);
                        allPlayersState.add(playerState);
                    }
                });
                return allPlayersState;
            }).andThen(asyncResult -> message.reply(asyncResult.result()));

            playersStatusMap.put(playerToRegister, ONLINE);
            broadcastStateChange(playerToRegister);
        });

        // Handler executed each time a client wants to propagate a status change
        vertx.eventBus().consumer(WS_PLAYER_STATUS, message -> {
            JsonObject statusUpdateRequest = new JsonObject(message.body().toString());
            String originPlayer = statusUpdateRequest.getString("originPlayer");
            String playerStatus = statusUpdateRequest.getString("playerStatus");
            System.out.printf("Received status update for %s to %s\n", originPlayer, playerStatus);

            // store the current status for the client which sent the message
            playersStatusMap.replace(originPlayer, playerStatus);

            if (playerStatus.equals(OFFLINE)) {
                Set<String> playerSockets = playersSocketMap.get(originPlayer);
                if (playerSockets.contains(statusUpdateRequest.getString("socketAddress")))
                    playersSocketMap.remove(originPlayer, playerSockets);
            }

            // broadcast tha status change
            broadcastStateChange(originPlayer);
        });

        // When the server stops receiving ping messages from a socket
        // for more than 5 seconds, a SOCKET_IDLE event is fired
        // and the following handler will be executed
        vertx.eventBus().consumer(WS_PLAYER_DISCONNECTION, message -> {
            JsonObject playerDisconnectionRequest = (JsonObject) message.body();
            String playerAddress = playerDisconnectionRequest.getString("socketAddress");

            // Search the username related to the socketAddress received,
            // remove the corresponding entry, mark his status as OFFLINE
            // and broadcast the status change
            vertx.executeBlocking(() -> {
                AtomicReference<String> possibleUsername = new AtomicReference<>();
                playersSocketMap.forEach((username, socketAddresses) -> {
                    if (socketAddresses.contains(playerAddress))
                        possibleUsername.set(username);
                });
                return possibleUsername;
            }).andThen(asyncResult -> {
                String usernameToDisconnect = asyncResult.result().get();
                if (usernameToDisconnect != null) {
                    playersStatusMap.replace(usernameToDisconnect, OFFLINE);
                    playersSocketMap.remove(usernameToDisconnect);
                    broadcastStateChange(usernameToDisconnect);
                }
            });
        });
    }

    private void broadcastStateChange(String originPlayer) {
        vertx.executeBlocking(() -> {
            String newStatus = playersStatusMap.get(originPlayer);
            playersStatusMap.entrySet().stream()
                    .filter(entry -> !entry.getKey().equals(originPlayer))
                    .forEach(entry -> sendPlayerStatus(originPlayer, entry.getKey(), newStatus));
            return null;
        });
    }

    private void notify(String matchID, String originPlayer, JsonObject notificationData) {
        vertx.executeBlocking(() -> {
            matchPlayersMap.get(matchID).stream()
                    .filter(playerInRoom -> !playerInRoom.equals(originPlayer))
                    .forEach(playerToBeNotified -> vertx.eventBus().publish(WS_EVENTS_ADDRESS + playerToBeNotified, notificationData.encode()));
            if (notificationData.getString("notificationType").equals(MessageType.MATCH_OVER.getType()))
                matchPlayersMap.remove(matchID);
            return null;
        });
    }

    private void sendPlayerStatus(String originPlayer, String playerToNotify, String status) {
        JsonObject message = new JsonObject()
                .put("originPlayer", originPlayer)
                .put("playerStatus", status);
        System.out.println("Sending [" + status + "] from " + originPlayer + " to " + playerToNotify);
        vertx.eventBus().send(WS_PLAYER_STATUS + playerToNotify, message.encode());
    }

    private void createCommunicationRoom(JsonArray matches) {
        vertx.executeBlocking(() -> {
            matches.stream()
                    .map(JsonObject.class::cast)
                    .filter(match -> match.getJsonObject("matchStatus").getString("matchState").equals("PLAYING"))
                    .forEach(activeMatch -> {
                        JsonArray matchPlayers = activeMatch.getJsonObject("matchStatus").getJsonArray("players");
                        String activeMatchID = activeMatch.getString("_id");
                        matchPlayersMap.computeIfAbsent(activeMatchID, matchID ->
                                matchPlayers.stream()
                                        .map(JsonObject.class::cast)
                                        .map(player -> player.getString("username"))
                                        .collect(Collectors.toSet()));
                    });
            return null;
        });
    }
}

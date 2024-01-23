package it.unibo.sd.project.webservice;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import it.unibo.sd.project.webservice.rabbit.MessageType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class NotificationService extends AbstractVerticle {
    private final Map<String, Set<String>> clientsSocket;

    public NotificationService() {
        clientsSocket = new HashMap<>(8);
    }

    @Override
    public void start() {
        vertx.eventBus().consumer(WebServer.WS_SERVICE_ADDRESS, message -> {
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
    }

    private void notify(String matchID, String originPlayer, JsonObject notificationData) {
        clientsSocket.get(matchID).stream()
                .filter(playerInRoom -> !playerInRoom.equals(originPlayer))
                .forEach(playerToBeNotified -> vertx.eventBus().publish(WebServer.BASE_ADDRESS + playerToBeNotified, notificationData.encode()));
        if (notificationData.getString("notificationType").equals(MessageType.MATCH_OVER.getType()))
            clientsSocket.remove(matchID);
    }

    private void createCommunicationRoom(JsonArray matches) {
        matches.stream()
                .map(JsonObject.class::cast)
                .filter(match -> match.getJsonObject("matchStatus").getString("matchState").equals("PLAYING"))
                .forEach(activeMatch -> {
                    JsonArray matchPlayers = activeMatch.getJsonObject("matchStatus").getJsonArray("players");
                    clientsSocket.computeIfAbsent(activeMatch.getString("_id"), matchID ->
                            matchPlayers.stream()
                                    .map(JsonObject.class::cast)
                                    .map(player -> player.getString("username"))
                                    .collect(Collectors.toSet()));
                });
    }
}

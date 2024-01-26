package it.unibo.sd.project.webservice.configuration;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import it.unibo.sd.project.webservice.WebServer;
import it.unibo.sd.project.webservice.rabbit.MessageType;

public class GameRoutesConfigurator extends RoutesConfigurator {
    public GameRoutesConfigurator(Vertx vertx) {
        super(vertx);
    }

    @Override
    public Router configure() {
        router.post("/searchMatch").blockingHandler(getRequestObject(
                (routingContext, matchRequest) -> {
                    JsonObject body = routingContext.body().asJsonObject();
                    boolean isMatchPrivate = body.getBoolean("secret");
                    matchRequest
                            .put("isPrivateMatch", isMatchPrivate);

                    getHandler(MessageType.SEARCH_MATCH, matchRequest.encode(),
                            (context, backendResponse) -> {
                                JsonObject responseBody = new JsonObject();
                                responseBody
                                        .put("resultMessage", backendResponse.getString("resultMessage"))
                                        .put("matchAccessCode", backendResponse.getString("matchAccessCode"));
                                context.response().end(responseBody.encode());
                                notifyNewMatch(
                                        matchRequest.getString("requesterUsername"),
                                        context.response().getStatusCode(),
                                        backendResponse.getJsonArray("matches"));
                            }).handle(routingContext);
                }
        ));

        router.delete("/searchMatch").blockingHandler(extractUsername(
                (routingContext, username) -> getHandler(MessageType.CANCEL_MATCH_SEARCH, username,
                        respondWithMessage()).handle(routingContext)
        ));

        router.post("/joinPrivateMatch").blockingHandler(getRequestObject(
                (routingContext, matchRequest) -> {
                    String matchAccessCode = routingContext.body().asJsonObject().getString("matchAccessCode");
                    matchRequest
                            .put("matchAccessCode", matchAccessCode);

                    getHandler(MessageType.JOIN_PRIVATE_MATCH, matchRequest.encode(),
                            (context, backendResponse) -> {
                                context.response().end(backendResponse.getString("resultMessage"));
                                notifyNewMatch(
                                        matchRequest.getString("requesterUsername"),
                                        context.response().getStatusCode(),
                                        backendResponse.getJsonArray("matches"));
                            }).handle(routingContext);
                }
        ));

        router.get("/getMatches").blockingHandler(extractUsername(
                (routingContext, username) -> getHandler(
                        MessageType.GET_MATCHES_OF_USER,
                        username,
                        (context, backendResponse) -> {
                            JsonObject responseBody = new JsonObject();
                            JsonArray matches = backendResponse.getJsonArray("matches");
                            responseBody
                                    .put("matches", matches)
                                    .put("pending", backendResponse.getBoolean("pending"));
                            context.response().end(responseBody.encode());

                            // since this method is always executed each time a client creates
                            // a match, reloads the page, search for a match, here is a good place
                            // to create a websocket room for every existing active matches
                            createCommunicationRoom(matches);
                        }).handle(routingContext)
        ));

        router.get("/getMatch").blockingHandler(extractUsername(
                (routingContext, username) -> {
                    String matchID = routingContext.queryParam("matchId").get(0);
                    getHandler(
                            MessageType.GET_MATCH,
                            matchID,
                            (context, backendResponse) -> {
                                JsonArray matches = backendResponse.getJsonArray("matches");
                                // create an object containing the profilePicture of each player
                                // in order to satisfy client's needs
                                JsonArray players = matches
                                        .getJsonObject(0)
                                        .getJsonObject("matchStatus")
                                        .getJsonArray("players");

                                JsonArray profilePics = new JsonArray();
                                for (int i = 0; i < players.getList().size(); i++)
                                    profilePics.add(new JsonObject()
                                            .put("username", players.getJsonObject(i).getString("username"))
                                            .put("picId", players.getJsonObject(i).getInteger("profilePictureID")));

                                JsonObject responseBody = new JsonObject();
                                responseBody
                                        .put("matches", matches)
                                        .put("profile_pics", profilePics);
                                context.response().end(responseBody.encode());
                            }).handle(routingContext);
                }
        ));

        router.put("/leaveMatch").blockingHandler(extractUsername(
                (routingContext, username) -> {
                    String matchID = routingContext.body().asJsonObject().getString("matchId");
                    JsonObject request = new JsonObject()
                            .put("requesterUsername", username)
                            .put("matchID", matchID);

                    getHandler(
                            MessageType.LEAVE_MATCH,
                            request.encode(),
                            (context, backendResponse) -> {
                                context.response().end(backendResponse.getString("resultMessage"));

                                sendRoomNotification(MessageType.MATCH_OVER, matchID, username);
                            }).handle(routingContext);
                }
        ));

        router.put("/doGuess").blockingHandler(extractUsername(
                (routingContext, username) -> {
                    JsonObject body = routingContext.body().asJsonObject();
                    String matchID = body.getString("matchId");
                    JsonArray sequence = body.getJsonArray("sequence");
                    JsonObject request = new JsonObject()
                            .put("requesterUsername", username)
                            .put("matchID", matchID)
                            .put("colorSequence", sequence);

                    getHandler(
                            MessageType.DO_GUESS,
                            request.encode(),
                            (context, backendResponse) -> {
                                JsonObject updatedStatus = backendResponse.getJsonObject("updatedStatus");
                                JsonObject submittedAttemptHints = backendResponse.getJsonObject("submittedAttemptHints");
                                JsonObject jsonResponse = new JsonObject()
                                        .put("rightP", submittedAttemptHints.getInteger("rightPositions"))
                                        .put("rightC", submittedAttemptHints.getInteger("rightColours"))
                                        .put("status", updatedStatus);
                                context.response().end(jsonResponse.encode());

                                boolean isMatchOver = updatedStatus
                                        .getString("matchState")
                                        .equals(MessageType.MATCH_OVER.getType());
                                sendRoomNotification(
                                        isMatchOver ? MessageType.MATCH_OVER : MessageType.NEW_MOVE,
                                        matchID, username);
                            }
                    ).handle(routingContext);
                }
        ));
        return router;
    }

    private void notifyNewMatch(String originPlayer, int statusCode, JsonArray createdMatch) {
        if (statusCode == 201) {
            createCommunicationRoom(createdMatch);
            String createdMatchID = createdMatch.stream()
                    .map(JsonObject.class::cast)
                    .map(match -> match.getString("_id"))
                    .findFirst().orElse("invalid ID");
            sendRoomNotification(MessageType.NEW_MATCH, createdMatchID, originPlayer);
        }
    }

    private void createCommunicationRoom(JsonArray matches) {
        JsonObject matchesArray = new JsonObject().put("matchesArray", matches);
        JsonObject notification = getNotificationRequest(MessageType.CREATE_ROOM, matchesArray);
        vertx.eventBus().send(WebServer.WS_SERVICE_ADDRESS, notification);
    }

    private void sendRoomNotification(MessageType notificationType, String matchID, String originPlayer) {
        JsonObject notificationData = new JsonObject()
                .put("matchID", matchID)
                .put("originPlayer", originPlayer);
        JsonObject notification = getNotificationRequest(notificationType, notificationData);
        vertx.eventBus().send(WebServer.WS_SERVICE_ADDRESS, notification);
    }

    private static JsonObject getNotificationRequest(MessageType notificationType, JsonObject data) {
        return new JsonObject()
                .put("notificationType", notificationType)
                .put("data", data);
    }
}

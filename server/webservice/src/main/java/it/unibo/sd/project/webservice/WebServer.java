package it.unibo.sd.project.webservice;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.http.Cookie;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.bridge.BridgeEventType;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;
import io.vertx.ext.web.handler.sockjs.SockJSBridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import it.unibo.sd.project.webservice.rabbit.MessageType;
import it.unibo.sd.project.webservice.rabbit.RPCClient;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;

public class WebServer extends AbstractVerticle {
    private final short LISTENING_PORT;
    private RPCClient gameBackend;

    public WebServer(short listeningPort) {
        LISTENING_PORT = listeningPort;
    }

    @Override
    public void start(Promise<Void> startPromise) {
        try {
            gameBackend = new RPCClient();
        } catch (IOException | TimeoutException e) {
            System.out.println(e.getMessage());
        }

        Router router = Router.router(vertx);

        router.route().handler(handleCORS());
        router.route("/eventbus/*").subRouter(getEventBusRouter());
        router.route("/api/*").subRouter(baseRouter());

        vertx
            .createHttpServer()
            .requestHandler(router)
            .listen(LISTENING_PORT, res -> {
                if (res.succeeded())
                    startPromise.complete();
                else startPromise.fail(res.cause());
            });
    }

    private Router baseRouter() {
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.route().consumes("application/json");

        JWTAuth jwtAccessProvider = getJwtAuthProvider("access.secret");

        // Non-protected routes
        router.route("/user/*").subRouter(getUserRouter());

        // Protected routes
        router.route("/protected/*")
                .handler(JWTAuthHandler.create(jwtAccessProvider))
                .failureHandler(this::manageAuthFailures)
                .subRouter(getProtectedRouter());

        return router;
    }

    private Router getUserRouter() {
        Router router = Router.router(vertx);

        router.post("/register").blockingHandler(getHandler(
                MessageType.REGISTER_USER, (routingContext, username) -> routingContext.response().end()));

        router.post("/login").blockingHandler(getHandler(
                MessageType.LOGIN_USER,
                (routingContext, response) -> {
                    JsonObject backendResponse = new JsonObject(response);
                    JsonObject user = backendResponse.getJsonObject("relatedUser");
                    String accessToken = backendResponse.getString("accessToken");
                    String refreshToken = user.getString("refreshToken");

                    long maxAge = 24 * 60 * 60; // one day expressed in seconds
                    Cookie cookie = Cookie.cookie("jwtRefreshToken", refreshToken)
                            .setMaxAge(maxAge)
                            .setHttpOnly(true);

                    JsonObject responseBody = new JsonObject();
                    responseBody
                            .put("accessToken", accessToken)
                            .put("profilePicID", user.getInteger("profilePictureID"))
                            .put("email", user.getString("email"));
                    routingContext.response().addCookie(cookie).end(responseBody.encode());
                }));

        router.get("/refreshToken").blockingHandler(checkRefreshTokenCookiePresence(
                        MessageType.REFRESH_ACCESS_TOKEN,
                        (context, response) -> {
                            JsonObject backendResponse = new JsonObject(response);
                            JsonObject user = backendResponse.getJsonObject("relatedUser");
                            String accessToken = backendResponse.getString("accessToken");
                            JsonObject responseBody = new JsonObject();
                            responseBody
                                    .put("username", user.getString("username"))
                                    .put("newAccessToken", accessToken)
                                    .put("profilePicID", user.getInteger("profilePictureID"))
                                    .put("email", user.getString("email"));
                            context.response().end(responseBody.encode());
                        }));

        router.get("/logout").blockingHandler(checkRefreshTokenCookiePresence(
                MessageType.LOGOUT_USER,
                (context, response) -> context.response().end(new JsonObject(response).getString("resultMessage"))
        ));

        return router;
    }

    private void manageAuthFailures(RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        Throwable cause = routingContext.failure().getCause();
        if (cause != null)
            if (cause.getMessage().contains("token expired"))
                response.setStatusCode(403).end("JWT token expired");
            else
                response.setStatusCode(400).end(cause.getMessage());
        else
            response.setStatusCode(500).end("Internal Server Error");
    }

    private Router getProtectedRouter() {
        Router router = Router.router(vertx);

        router.route("/game/*").subRouter(getGameRouter());
        // router.route("/settings/*").subRouter(getSettingsRouter());
        // router.route("/stats/*").subRouter(getStatsRouter());

        return router;
    }

    private Router getGameRouter() {
        Router router = Router.router(vertx);

        router.post("/searchMatch").blockingHandler(extractUsername(
                (routingContext, username) -> {
                    JsonObject body = routingContext.body().asJsonObject();
                    boolean isMatchPrivate = body.getBoolean("secret");
                    JsonObject matchRequest = new JsonObject();
                    matchRequest
                            .put("requesterUsername", username)
                            .put("isPrivateMatch", isMatchPrivate);

                    getHandler(MessageType.SEARCH_MATCH, matchRequest.encode(),
                            (context, response) -> {
                                JsonObject backendResponse = new JsonObject(response);
                                JsonObject responseBody = new JsonObject();
                                responseBody.put("matchAccessCode", backendResponse.getString("matchAccessCode"));
                                context.response().end(responseBody.encode());
                            }).handle(routingContext);
                }
        ));

        router.post("/joinPrivateMatch").blockingHandler(extractUsername(
                (routingContext, username) -> {
                    String matchAccessCode = routingContext.body().asJsonObject().getString("matchAccessCode");
                    JsonObject matchRequest = new JsonObject();
                    matchRequest
                            .put("requesterUsername", username)
                            .put("matchAccessCode", matchAccessCode);

                    getHandler(MessageType.JOIN_PRIVATE_MATCH, matchRequest.encode(),
                            (context, response) -> {
                                JsonObject backendResponse = new JsonObject(response);
                                context.response().end(backendResponse.getString("resultMessage"));
                            }).handle(routingContext);
                }
        ));

        router.get("/getMatches").blockingHandler(extractUsername(
                (routingContext, username) -> getHandler(
                        MessageType.GET_MATCHES_OF_USER,
                        username,
                        (context, response) -> {
                            JsonObject backendResponse = new JsonObject(response);
                            JsonObject responseBody = new JsonObject();
                            responseBody
                                    .put("matches", backendResponse.getJsonArray("matches"))
                                    .put("pending", backendResponse.getBoolean("pending"));
                            context.response().end(responseBody.encode());
                        }).handle(routingContext)
        ));

        router.get("/getMatch").blockingHandler(extractUsername(
                (routingContext, username) -> {
                    String matchID = routingContext.queryParam("matchId").get(0);
                    getHandler(
                            MessageType.GET_MATCH,
                            matchID,
                            (context, response) -> {
                                JsonObject backendResponse = new JsonObject(response);
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
                            (context, response) -> {
                                JsonObject backendResponse = new JsonObject(response);
                                context.response().end(backendResponse.getString("resultMessage"));
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
                            (context, response) -> {
                                JsonObject backendResponse = new JsonObject(response);
                                JsonObject submittedAttemptHints = backendResponse.getJsonObject("submittedAttemptHints");
                                JsonObject jsonResponse = new JsonObject()
                                        .put("rightP", submittedAttemptHints.getInteger("rightPositions"))
                                        .put("rightC", submittedAttemptHints.getInteger("rightColours"))
                                        .put("status", new JsonObject()); // the client expects this but it may be useless
                                context.response().end(jsonResponse.encode());
                            }
                    ).handle(routingContext);
                }
        ));

        return router;
    }

    /*private Router getSettingsRouter() {
        Router router = Router.router(vertx);



        return router;
    }

    private Router getStatsRouter() {
        Router router = Router.router(vertx);



        return router;
    }*/

    private Handler<RoutingContext> extractUsername(BiConsumer<RoutingContext, String> consumer) {
        return routingContext -> {
            String username = routingContext.user().subject();
            if (username == null || username.isBlank())
                routingContext.response().setStatusCode(400).end();
            else
                consumer.accept(routingContext, username);
        };
    }

    private Handler<RoutingContext> checkRefreshTokenCookiePresence(MessageType messageType,
                                                                    BiConsumer<RoutingContext, String> consumer) {
        return routingContext -> {
            String cookieName = "jwtRefreshToken";
            Cookie cookie = routingContext.request().getCookie(cookieName);
            if (cookie != null) {
                String refreshToken = cookie.getValue();
                if (messageType.getType().equals(MessageType.LOGOUT_USER.getType()))
                    routingContext.response().removeCookies(cookieName);
                getHandler(messageType, refreshToken, consumer).handle(routingContext);
            }
            else {
                int statusCode = messageType.getType().equals(MessageType.LOGOUT_USER.getType()) ? 204 : 401;
                routingContext
                        .response()
                        .setStatusCode(statusCode)
                        .end();
            }
        };
    }

    private JWTAuth getJwtAuthProvider(String symmetricKey) {
        //TODO: change this password with a random generated string inside an environment variable
        JWTAuthOptions jwtAuthOptions = new JWTAuthOptions()
                .addPubSecKey(new PubSecKeyOptions()
                        .setAlgorithm("HS256")
                        .setBuffer(symmetricKey));
        return JWTAuth.create(vertx, jwtAuthOptions);
    }

    private Handler<RoutingContext> getHandler(MessageType messageType, BiConsumer<RoutingContext, String> consumer) {
        return routingContext -> getHandler(messageType, routingContext.body().asString(), consumer).handle(routingContext);
    }


    private Handler<RoutingContext> getHandler(MessageType messageType, String message,
                                               BiConsumer<RoutingContext, String> consumer) {
        return routingContext -> gameBackend.call(messageType, message, res -> {
            JsonObject backendResponse = new JsonObject(res);
            int statusCode = backendResponse.getInteger("statusCode");
            routingContext
                    .response()
                    .putHeader("Content-Type", "application/json")
                    .setStatusCode(statusCode);
            if (statusCode >= 200 && statusCode <= 204)
                consumer.accept(routingContext, backendResponse.encode());
            else
                routingContext.response().end(backendResponse.getString("resultMessage"));
        });
    }

    private Handler<RoutingContext> handleCORS() {
        List<String> allowedOrigins = List.of("http://localhost", "http://localhost:3000");
        Set<String> allowedHeaders = Set.of("Content-Type", "Authorization", "origin", "Accept");
        Set<HttpMethod> allowedMethods = Set.of(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT);

        return CorsHandler.create()
                .addOrigins(allowedOrigins)
                .allowedHeaders(allowedHeaders)
                .allowedMethods(allowedMethods)
                .allowCredentials(true);
    }

    private Router getEventBusRouter() {
        SockJSBridgeOptions options = new SockJSBridgeOptions();
        options.addInboundPermitted(new PermittedOptions().setAddressRegex("huesle.*"));
        options.addOutboundPermitted(new PermittedOptions().setAddressRegex("huesle.*"));
        SockJSHandler sockJSHandler = SockJSHandler.create(vertx);
        return sockJSHandler.bridge(options, event -> {
            if (event.type() == BridgeEventType.SOCKET_CREATED) {
                System.out.println("new socket created " + event);
            }
            event.complete(true);
        });
    }
}

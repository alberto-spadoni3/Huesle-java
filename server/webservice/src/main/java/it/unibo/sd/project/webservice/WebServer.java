package it.unibo.sd.project.webservice;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.http.Cookie;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.bridge.BridgeEventType;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.HttpException;
import io.vertx.ext.web.handler.JWTAuthHandler;
import io.vertx.ext.web.handler.sockjs.SockJSBridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import it.unibo.sd.project.webservice.rabbit.MessageType;
import it.unibo.sd.project.webservice.rabbit.RPCClient;
import java.io.IOException;
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

        // TODO manage CORS when needed
        // router.route().handler(CorsHandler.create().addOrigin("http://localhost"));

        router.route("/eventbus/*").subRouter(getEventBusRouter());
        router.route("/api/user/*").subRouter(getUserRouter());

        JWTAuth jwtAccessProvider = getJwtAuthProvider("access.secret");
        router.route("/api/protected/*")
                .handler(JWTAuthHandler.create(jwtAccessProvider))
                .failureHandler(this::manageAuthFailures);
        router.route("/api/protected/db").handler(c -> {
            c.response().end("Hi " + c.user().subject());
        });

        vertx
            .createHttpServer()
            .requestHandler(router)
            .listen(LISTENING_PORT, res -> {
                if (res.succeeded())
                    startPromise.complete();
                else startPromise.fail(res.cause());
            });
    }

    private void manageAuthFailures(RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        Throwable cause = routingContext.failure().getCause();
        if (cause != null)
            if (cause.getMessage().contains("token expired"))
                response.setStatusCode(403).end("JWT token expired");
            else
                response.setStatusCode(400).end(cause.getMessage())
;        else
            response.setStatusCode(500).end("Internal Server Error");
    }

    private Router getUserRouter() {
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.route().consumes("application/json");

        router.post("/register").handler(getHandler(
                MessageType.REGISTER_USER, (routingContext, username) -> routingContext.response().end()));

        router.post("/login").handler(getHandler(
                MessageType.LOGIN_USER,
                (routingContext, response) -> {
                    JsonObject backendResponse = new JsonObject(response);
                    if (routingContext.response().getStatusCode() == 200) {
                        // Authentication with JWT
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
                    } else
                        routingContext.response().end(backendResponse.getString("resultMessage"));
                }));
        return router;
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
        return routingContext -> gameBackend.call(messageType, routingContext.body().asString(), res -> {
            JsonObject backendResponse = new JsonObject(res);
            routingContext
                    .response()
                    .putHeader("Content-Type", "application/json")
                    .setStatusCode(backendResponse.getInteger("statusCode"));
            consumer.accept(routingContext, backendResponse.encode());
        });
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

package it.unibo.sd.project.webservice;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
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
import io.vertx.ext.web.handler.sockjs.BridgeEvent;
import io.vertx.ext.web.handler.sockjs.SockJSBridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import it.unibo.sd.project.webservice.configuration.GameRoutesConfigurator;
import it.unibo.sd.project.webservice.configuration.SettingsRoutesConfigurator;
import it.unibo.sd.project.webservice.configuration.StatsRoutesConfigurator;
import it.unibo.sd.project.webservice.configuration.UserRoutesConfigurator;

import java.util.List;
import java.util.Set;

public class WebServer extends AbstractVerticle {
    private final short LISTENING_PORT;
    public static final String BASE_ADDRESS = "huesle.";

    public WebServer(short listeningPort) {
        LISTENING_PORT = listeningPort;
    }

    @Override
    public void start(Promise<Void> serverStart) {
        Router router = Router.router(vertx);

        router.route().handler(handleCORS());
        router.route("/eventbus/*").subRouter(getEventBusRouter());
        router.route("/api/*").subRouter(baseRouter());

        vertx
            .createHttpServer()
            .requestHandler(router)
            .listen(LISTENING_PORT, res -> {
                if (res.succeeded()) {
                    var serviceStart = vertx.deployVerticle(new NotificationService());
                    serviceStart.onSuccess(e -> {
                        System.out.println(NotificationService.class.getSimpleName() + " is running!");
                        serverStart.complete();
                    });
                } else serverStart.fail(res.cause());
            });
    }

    private Router baseRouter() {
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.route().consumes("application/json");
        JWTAuth jwtAccessProvider = getJwtAuthProvider();

        // Non-protected routes
        UserRoutesConfigurator userRoutesConfigurator = new UserRoutesConfigurator(vertx, jwtAccessProvider);
        router.route("/user/*").subRouter(userRoutesConfigurator.configure());

        // Protected routes
        router.route("/protected/*")
            .handler(JWTAuthHandler.create(jwtAccessProvider))
            .failureHandler(this::manageAuthFailures)
            .subRouter(getProtectedRouter());

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

        GameRoutesConfigurator gameRoutesConfigurator = new GameRoutesConfigurator(vertx);
        router.route("/game/*").subRouter(gameRoutesConfigurator.configure());

        SettingsRoutesConfigurator settingsRoutesConfigurator = new SettingsRoutesConfigurator(vertx);
        router.route("/settings/*").subRouter(settingsRoutesConfigurator.configure());

        StatsRoutesConfigurator statsRoutesConfigurator = new StatsRoutesConfigurator(vertx);
        router.route("/stats/*").subRouter(statsRoutesConfigurator.configure());

        return router;
    }

    private JWTAuth getJwtAuthProvider() {
        String symmetricKey = System.getenv("ACCESS_TOKEN_SECRET");
        JWTAuthOptions jwtAuthOptions = new JWTAuthOptions()
            .addPubSecKey(new PubSecKeyOptions()
                .setAlgorithm("HS256")
                .setBuffer(symmetricKey));
        return JWTAuth.create(vertx, jwtAuthOptions);
    }

    private Handler<RoutingContext> handleCORS() {
        List<String> allowedOrigins = List.of("http://localhost", "http://localhost:3000");
        Set<String> allowedHeaders = Set.of("Content-Type", "Authorization", "origin", "Accept");
        Set<HttpMethod> allowedMethods = Set.of(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE);

        return CorsHandler.create()
            .addOrigins(allowedOrigins)
            .allowedHeaders(allowedHeaders)
            .allowedMethods(allowedMethods)
            .allowCredentials(true);
    }

    private Router getEventBusRouter() {
        int pingTimeout = 10000;
        SockJSBridgeOptions options = new SockJSBridgeOptions()
            .addInboundPermitted(new PermittedOptions().setAddressRegex(BASE_ADDRESS + "*"))
            .addOutboundPermitted(new PermittedOptions().setAddressRegex(BASE_ADDRESS + "*"))
            .setPingTimeout(pingTimeout);

        SockJSHandler sockJSHandler = SockJSHandler.create(vertx);
        return sockJSHandler.bridge(options, event -> {
            if (event.type() == BridgeEventType.SOCKET_CREATED) {
                System.out.println("New socket created with URI: " + event.socket().uri());
            }

            if (event.type() == BridgeEventType.SOCKET_CLOSED || event.type() == BridgeEventType.SOCKET_IDLE) {
                disconnectPlayer(event);
                if (event.type() == BridgeEventType.SOCKET_IDLE)
                    System.out.println("The socket " + event.socket().uri() +
                        " didn't send any ping message for more than " + pingTimeout +
                        " seconds, so it's considered to be idle and will be closed.");
                else
                    System.out.println("The socket " + event.socket().uri() + " was closed");
            }

            event.complete(true);
        }).errorHandler(1, routingContext -> System.out.println(routingContext.failure().getMessage()));
    }

    private void disconnectPlayer(BridgeEvent event) {
        JsonObject disconnectionMessage = new JsonObject().put("socketAddress", event.socket().uri());
        vertx.eventBus().send(NotificationService.WS_PLAYER_DISCONNECTION, disconnectionMessage);
    }
}

package it.unibo.sd.project.webservice;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.bridge.BridgeEventType;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.sockjs.SockJSBridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import it.unibo.sd.project.webservice.rabbit.MessageType;
import it.unibo.sd.project.webservice.rabbit.RPCClient;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class WebServer extends AbstractVerticle {
    private final short LISTENING_PORT;
    private RPCClient gameBackend;
    private HttpServer httpServer;

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

        httpServer = vertx
                .createHttpServer()
                .requestHandler(router)
                .listen(LISTENING_PORT, res -> {
                    if (res.succeeded())
                        startPromise.complete();
                    else startPromise.fail(res.cause());
                });
    }

    private Router getUserRouter() {
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.route().consumes("application/json");

        router.post("/register").handler(getHandler(MessageType.REGISTER_USER));
        router.post("/login").handler(getHandler(MessageType.LOGIN_USER));

        return router;
    }

    private Handler<RoutingContext> getHandler(MessageType messageType) {
        return routingContext -> {
            gameBackend.call(messageType, routingContext.body().asString(), backendResponse -> {
                JsonObject response = new JsonObject(backendResponse);
                routingContext
                        .response()
                        .setStatusCode(response.getInteger("statusCode"))
                        .putHeader("Content-Type", "application/json")
                        .end(response.getString("resultMessage"));
            });
        };
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

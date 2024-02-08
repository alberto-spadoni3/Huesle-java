package it.unibo.sd.project.webservice.configuration;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import it.unibo.sd.project.webservice.rabbit.MessageType;
import it.unibo.sd.project.webservice.rabbit.RPCClient;

import java.util.function.BiConsumer;

public abstract class RoutesConfigurator {
    protected Router router;
    protected final Vertx vertx;
    protected RPCClient gameBackend;

    protected RoutesConfigurator(Vertx vertx) {
        this.vertx = vertx;
        router = Router.router(vertx);
        gameBackend = RPCClient.getInstance();
    }

    public abstract Router configure();

    protected Handler<RoutingContext> backendHandler(MessageType messageType, BiConsumer<RoutingContext, JsonObject> consumer) {
        return routingContext -> backendHandler(messageType, routingContext.body().asString(), consumer).handle(routingContext);
    }

    protected Handler<RoutingContext> backendHandler(MessageType messageType, String message,
                                                     BiConsumer<RoutingContext, JsonObject> consumer) {
        return routingContext -> gameBackend.call(messageType, message, res -> {
            JsonObject backendResponse = new JsonObject(res);
            int statusCode = backendResponse.getInteger("statusCode");
            routingContext
                    .response()
                    .putHeader("Content-Type", "application/json")
                    .setStatusCode(statusCode);
            if (statusCode >= 200 && statusCode <= 204)
                consumer.accept(routingContext, backendResponse);
            else
                respondWithMessage().accept(routingContext, backendResponse);
        });
    }

    protected Handler<RoutingContext> extractUsername(BiConsumer<RoutingContext, String> consumer) {
        return routingContext -> {
            String username = routingContext.user().subject();
            if (username == null || username.isBlank())
                routingContext.response().setStatusCode(400).end();
            else
                consumer.accept(routingContext, username);
        };
    }

    protected Handler<RoutingContext> getRequestObject(BiConsumer<RoutingContext, JsonObject> consumer) {
        return routingContext -> extractUsername(
                (context, username) ->
                    consumer.accept(
                        routingContext,
                        new JsonObject().put("requesterUsername", username))
        ).handle(routingContext);
    }

    protected BiConsumer<RoutingContext, JsonObject> respondWithMessage() {
        return (routingContext, backendResponse) ->
                routingContext.response().end(
                new JsonObject()
                        .put("resultMessage", backendResponse.getString("resultMessage"))
                        .encode());
    }
}

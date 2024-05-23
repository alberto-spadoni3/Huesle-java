package it.unibo.sd.project.webservice.configuration;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import it.unibo.sd.project.webservice.rabbit.MessageType;

public class StatsRoutesConfigurator extends RoutesConfigurator {
    public StatsRoutesConfigurator(Vertx vertx) {
        super(vertx);
    }

    @Override
    public Router configure() {
        router.get("/userStats").blockingHandler(extractUsername(
            (routingContext, username) -> backendHandler(
                MessageType.GET_USER_STATS,
                username,
                (context, backendResponse) ->
                    context.response().end(backendResponse.getJsonObject("userStats").encode())
            ).handle(routingContext)
        ));

        return router;
    }
}

package it.unibo.sd.project.webservice;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import it.unibo.sd.project.mastermind.GameManager;
import it.unibo.sd.project.mastermind.model.mongo.DBSingleton;
import it.unibo.sd.project.mastermind.UserManager;

public class ServicesLauncher extends AbstractVerticle {
    private final short PORT;

    public ServicesLauncher(short port) {
        PORT = port;
    }

    @Override
    public void start(Promise<Void> verticleStart) {
        boolean forTesting = true;
        new UserManager(forTesting);
        new GameManager(forTesting);
        vertx.deployVerticle(new WebServer(PORT), res -> {
            if (res.succeeded())
                verticleStart.complete();
            else System.out.println(res.cause().getMessage());
        });
        DBSingleton.getTestDatabase().drop();
    }
}

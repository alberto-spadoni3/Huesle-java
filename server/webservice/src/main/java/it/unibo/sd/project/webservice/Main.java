package it.unibo.sd.project.webservice;

import io.vertx.core.Vertx;

public class Main {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        short listeningPort = 8080;

        var future = vertx.deployVerticle(new WebServer(listeningPort));
        future.andThen(e -> System.out.println("Server listening on port " + listeningPort));
    }
}

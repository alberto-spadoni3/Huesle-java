package it.unibo.sd.project.webservice.configuration;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import it.unibo.sd.project.webservice.rabbit.MessageType;

public class SettingsRoutesConfigurator extends RoutesConfigurator {
    public SettingsRoutesConfigurator(Vertx vertx) {
        super(vertx);
    }

    @Override
    public Router configure() {
        router.get("/profileSettings").blockingHandler(extractUsername(
                (routingContext, username) -> getHandler(
                        MessageType.GET_SETTINGS,
                        username,
                        (context, backendResponse) -> {
                            JsonObject settings = backendResponse
                                    .getJsonObject("relatedUser")
                                    .getJsonObject("accessibilitySettings");
                            context.response().end(getSettings(
                                    settings.getBoolean("darkMode"),
                                    settings.getBoolean("colorblindMode")).encode());
                        }
                ).handle(routingContext)
        ));
        router.put("/profileSettings").blockingHandler(getRequestObject(
                (routingContext, request) -> {
                    JsonObject body = routingContext.body().asJsonObject();
                    request.put("accessibilitySettings", getSettings(
                                        body.getBoolean("darkMode"),
                                        body.getBoolean("colorblindMode")));
                    getHandler(
                            MessageType.UPDATE_SETTINGS,
                            request.encode(),
                            (context, backendResponse) -> {
                                JsonObject result = new JsonObject()
                                        .put("resultMessage", backendResponse.getString("resultMessage"));
                                context.response().end(result.encode());
                            }).handle(routingContext);
                }
        ));

        router.put("/profilePicture").blockingHandler(getRequestObject(
                (routingContext, request) -> {
                    JsonObject body = routingContext.body().asJsonObject();
                    request.put("profilePictureID", body.getInteger("profilePicID"));

                    getHandler(
                            MessageType.UPDATE_PROFILE_PIC,
                            request.encode(),
                            (context, backendResponse) -> {
                                JsonObject result = new JsonObject()
                                        .put("resultMessage", backendResponse.getString("resultMessage"));
                                context.response().end(result.encode());
                            }
                    ).handle(routingContext);
                }
        ));

        router.post("/updateEmail").blockingHandler(getRequestObject(
                (routingContext, request) -> {
                    JsonObject body = routingContext.body().asJsonObject();
                    request.put("newEmail", body.getString("newEmail"));
                    getHandler(
                            MessageType.UPDATE_EMAIL,
                            request.encode(),
                            respondWithMessage()
                    ).handle(routingContext);
                }
        ));

        router.post("/updatePassword").blockingHandler(getRequestObject(
                (routingContext, request) -> {
                    JsonObject body = routingContext.body().asJsonObject();
                    request.put("oldPassword", body.getString("oldPassword"))
                           .put("newPassword", body.getString("newPassword"));
                    getHandler(
                            MessageType.UPDATE_PASSWORD,
                            request.encode(),
                            respondWithMessage()
                    ).handle(routingContext);
                }
        ));

        return router;
    }

    private static JsonObject getSettings(boolean daskMode, boolean colorblindMode) {
        return new JsonObject()
                .put("darkMode", daskMode)
                .put("colorblindMode", colorblindMode);
    }
}

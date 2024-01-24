package it.unibo.sd.project.webservice.configuration;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.Cookie;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import it.unibo.sd.project.webservice.rabbit.MessageType;

import java.util.function.BiConsumer;

public class UserRoutesConfigurator extends RoutesConfigurator {
    public UserRoutesConfigurator(Vertx vertx) {
        super(vertx);
    }

    @Override
    public Router configure() {
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
                (context, response) -> {
                    context.response().removeCookies("jwtRefreshToken");
                    context.response().end(new JsonObject(response).getString("resultMessage"));
                }
        ));

        return router;
    }

    private Handler<RoutingContext> checkRefreshTokenCookiePresence(MessageType messageType,
                                                                    BiConsumer<RoutingContext, String> consumer) {
        return routingContext -> {
            String cookieName = "jwtRefreshToken";
            Cookie cookie = routingContext.request().getCookie(cookieName);
            if (cookie != null) {
                String refreshToken = cookie.getValue();
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

}

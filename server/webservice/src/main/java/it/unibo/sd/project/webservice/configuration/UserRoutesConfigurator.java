package it.unibo.sd.project.webservice.configuration;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.Cookie;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.JWTAuthHandler;
import it.unibo.sd.project.webservice.configuration.utils.HttpStatusCodes;
import it.unibo.sd.project.webservice.rabbit.MessageType;

import java.util.function.BiConsumer;

public class UserRoutesConfigurator extends RoutesConfigurator {
    private final JWTAuth jwtAccessProvider;

    public UserRoutesConfigurator(Vertx vertx, JWTAuth jwtAccessProvider) {
        super(vertx);
        this.jwtAccessProvider = jwtAccessProvider;
    }

    @Override
    public Router configure() {
        router.post("/register").blockingHandler(backendHandler(
            MessageType.REGISTER_USER, respondWithMessage()));

        router.post("/login").blockingHandler(backendHandler(
            MessageType.LOGIN_USER,
            (routingContext, backendResponse) -> {
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
            (context, backendResponse) -> {
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
            (context, backendResponse) -> {
                context.response().removeCookies("jwtRefreshToken");
                respondWithMessage().accept(context, backendResponse);
            }
        ));

        router.delete("/delete")
            // since this router belongs to a non-protected-route,
            // we first need to authenticate the user using JWT
            .blockingHandler(JWTAuthHandler.create(jwtAccessProvider))
            .blockingHandler(extractUsername(
                (routingContext, username) -> backendHandler(
                    MessageType.DELETE_USER,
                    username,
                    respondWithMessage()
                ).handle(routingContext)
            ));

        return router;
    }

    private Handler<RoutingContext> checkRefreshTokenCookiePresence(MessageType messageType,
                                                                    BiConsumer<RoutingContext, JsonObject> consumer) {
        return routingContext -> {
            String cookieName = "jwtRefreshToken";
            Cookie cookie = routingContext.request().getCookie(cookieName);
            if (cookie != null) {
                String refreshToken = cookie.getValue();
                backendHandler(messageType, refreshToken, consumer).handle(routingContext);
            } else {
                int statusCode = messageType.getType().equals(
                    MessageType.LOGOUT_USER.getType()) ? HttpStatusCodes.NO_CONTENT : HttpStatusCodes.UNAUTHORIZED;
                routingContext
                    .response()
                    .setStatusCode(statusCode)
                    .end();
            }
        };
    }
}

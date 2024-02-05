package it.unibo.sd.project.webservice;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.*;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import it.unibo.sd.project.mastermind.controllers.UserController;
import it.unibo.sd.project.mastermind.model.mongo.DBSingleton;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WebServiceTests {
    private final HttpClient client;
    private final short listeningPort;
    private final String username;
    private final String clearPassword;
    private String accessToken;
    private Cookie refreshTokenCookie;

    public WebServiceTests(Vertx vertx) {
        this.listeningPort = (short) 8080;
        this.username = "Aldo";
        this.clearPassword = "NasaIsCool123!";
        HttpClientOptions options = new HttpClientOptions()
                .setDefaultHost("localhost")
                .setDefaultPort(listeningPort);
        client = vertx.createHttpClient(options);
    }

    @BeforeAll
    void startWebServices(Vertx vertx, VertxTestContext testContext) {
        vertx.deployVerticle(new ServicesLauncher(listeningPort), testContext.succeeding(e -> {
            System.out.println("All services are running!");
            testContext.completeNow();
        }));
        registerUser(testContext.succeedingThenComplete());
    }

    @Test
    @Order(1)
    void loginUser(VertxTestContext testContext) {
        Checkpoint responseSucceeded = testContext.checkpoint();
        Checkpoint responseBodyProcessed = testContext.checkpoint();
        JsonObject requestBody = new JsonObject()
                .put("username", username)
                .put("password", clearPassword);

        client
            .request(HttpMethod.POST, "/api/user/login")
            .compose(request -> request.send(requestBody.encode())
            .compose(response -> {
                assertEquals(200, response.statusCode());
                if (response.statusCode() == 200) {
                    responseSucceeded.flag();
                    this.refreshTokenCookie = checkRefreshTokenCookiePresence(response.cookies());
                    // check the refreshToken cookie presence
                    assertNotNull(refreshTokenCookie);
                    return response.body();
                }
                else return getFailedFuture(response);
            }))
            .onSuccess(responseBody -> testContext.verify(() -> {
                // save the accessToken for future requests that require authentication
                accessToken = responseBody.toJsonObject().getString("accessToken");
                assertNotNull(accessToken);
                responseBodyProcessed.flag();
            }))
            .onFailure(error -> testContext.failNow(error.getMessage()));
    }

    @Test
    @Order(2)
    void requestProtectedRoute(VertxTestContext testContext) {
        client
            .request(HttpMethod.GET, "/api/protected/game/getMatches")
            .compose(request -> request.putHeader("Authorization", "Bearer " + accessToken).send())
            .compose(response -> {
                if (response.statusCode() == 200)
                    return response.body();
                else return getFailedFuture(response);
            }).onSuccess(responseBody -> {
                // just check if the response body is present and has the supposed fields
                assertTrue(responseBody.toJsonObject().containsKey("matches"));
                assertTrue(responseBody.toJsonObject().containsKey("pending"));
                testContext.completeNow();
            }).onFailure(error -> testContext.failNow(error.getMessage()));
    }

    @Test
    @Order(3)
    void logoutUser(VertxTestContext testContext) {
        Checkpoint cookiePresenceVerified = testContext.checkpoint();
        Checkpoint responseBodyProcessed = testContext.checkpoint();

        client
                .request(HttpMethod.GET, "/api/user/logout")
                .compose(request -> request.putHeader(
                        "Cookie",
                        refreshTokenCookie.getName() + "=" + refreshTokenCookie.getValue()).send())
                .compose(response -> {
                    int statusCode = response.statusCode();
                    if (statusCode <= 204) {
                        Cookie refreshTokenCookie = checkRefreshTokenCookiePresence(response.cookies());
                        assertNull(refreshTokenCookie);
                        cookiePresenceVerified.flag();
                        return response.body();
                    }
                    else return getFailedFuture(response);
                }).onSuccess(responseBody -> {
                    if (!responseBody.toString().isEmpty()) {
                        // check that the response body is coherent
                        String resultMessage = responseBody.toJsonObject().getString("resultMessage");
                        assertTrue(resultMessage.contains(this.username));
                    }
                    responseBodyProcessed.flag();
                }).onFailure(error -> testContext.failNow(error.getMessage()));
    }

    private static Future<Buffer> getFailedFuture(HttpClientResponse response) {
        return Future.failedFuture(new RuntimeException("[" + response.statusCode() + "] " + response.statusMessage()));
    }

    private void registerUser(Handler<AsyncResult<Void>> handler) {
        String email = "aldo@nasa.gov.us";
        JsonObject requestBody = new JsonObject()
                .put("username", username)
                .put("email", email)
                .put("password", clearPassword);
        UserController userController = new UserController(DBSingleton.getInstance().getTestDatabase());
        JsonObject response = new JsonObject(userController.registerUser().apply(requestBody.encode()));
        if (response.getInteger("statusCode") == 201)
            handler.handle(Future.succeededFuture());
        else {
            String resultMessage = response.getString("resultMessage");
            throw new RuntimeException("Preliminary user registration went wrong: " + resultMessage);
        }
    }

    private Cookie checkRefreshTokenCookiePresence(List<String> cookies) {
        for (String cookie : cookies)
            if (cookie.startsWith("jwtRefreshToken")) {
                String refreshCookie = cookie.split(";")[0];
                String[] cookieParts = refreshCookie.split("=");
                if (cookieParts.length == 2) {
                    String refreshCookieName = cookieParts[0];
                    String refreshCookieValue = cookieParts[1];
                    return Cookie.cookie(refreshCookieName, refreshCookieValue);
                }
            }
        return null;
    }
}

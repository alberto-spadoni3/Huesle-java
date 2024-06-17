package it.unibo.sd.project.mastermind;

import com.mongodb.client.model.Filters;
import it.unibo.sd.project.mastermind.controllers.utils.HttpStatusCodes;
import it.unibo.sd.project.mastermind.model.mongo.DBManager;
import it.unibo.sd.project.mastermind.model.mongo.DBSingleton;
import it.unibo.sd.project.mastermind.model.result.OperationResult;
import it.unibo.sd.project.mastermind.model.result.UserOperationResult;
import it.unibo.sd.project.mastermind.model.user.Player;
import it.unibo.sd.project.mastermind.presentation.Presentation;
import it.unibo.sd.project.mastermind.rabbit.MessageType;
import it.unibo.sd.project.mastermind.rabbit.RPCClient;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserOperationsTests {
    private ExecutorService executorService;
    private RPCClient client;
    private DBManager<Player> userDB;
    private String username;
    private String email;
    private String clearPassword;
    private String accessToken;

    @BeforeAll
    public void setUpTests() throws IOException, TimeoutException, InterruptedException {
        new UserManager(true);
        // this 10 millisecond waiting time guarantees that the RPCServer inside
        // UserManager has started before tests execution
        Thread.sleep(50);
        client = new RPCClient();
        var testDatabase = DBSingleton.getTestDatabase();
        // Drop the possible database to avoid conflicts
        testDatabase.drop();
        userDB = new DBManager<>(testDatabase, "users", "username", Player.class);
        username = "albisyx";
        email = "albisyx@protonmail.ch";
        clearPassword = "passwd123!";
        accessToken = "";
    }

    @BeforeEach
    public void initExecutor() {
        executorService = Executors.newSingleThreadExecutor();
    }

    @AfterEach
    public void stopExecutor() {
        executorService.shutdown();
    }

    @Test
    @Order(1)
    @DisplayName("Test the correct registration process")
    void userRegistrationTest() throws Exception {
        OperationResult result = callAsync(
            MessageType.REGISTER_USER,
            getRegistrationJson(username, email, clearPassword));
        assertEquals(HttpStatusCodes.CREATED, result.getStatusCode());
    }

    @Test
    @Order(2)
    @DisplayName("Check if the just created user is actually in the database")
    void userIsInDB() throws Exception {
        Optional<Player> createdUser = userDB.getDocumentByField("username", username);
        assertTrue(createdUser.isPresent());
        assertEquals(username, createdUser.get().getUsername());
        assertEquals(email, createdUser.get().getEmail());
        byte defaultProfilePictureID = 0;
        assertEquals(defaultProfilePictureID, createdUser.get().getProfilePictureID());
        boolean defaultDarkModeValue = true;
        assertEquals(defaultDarkModeValue, createdUser.get().getSettings().isDarkMode());
        assertFalse(createdUser.get().isDisabled());
    }

    @Test
    @Order(3)
    @DisplayName("Try to register a user that is already been created")
    void duplicateUserTest() throws Exception {
        OperationResult result = callAsync(
            MessageType.REGISTER_USER,
            getRegistrationJson(username + "1", email, clearPassword));

        assertEquals(HttpStatusCodes.CONFLICT, result.getStatusCode());
        String EMAIL_ALREADY_EXISTS_MESSAGE = "The email address is already in use";
        assertEquals(EMAIL_ALREADY_EXISTS_MESSAGE, result.getResultMessage());


        result = callAsync(
            MessageType.REGISTER_USER,
            getRegistrationJson(username, "S" + email, clearPassword));

        assertEquals(HttpStatusCodes.CONFLICT, result.getStatusCode());
        String USERNAME_ALREADY_EXISTS_MESSAGE = "The username is already in use";
        assertEquals(USERNAME_ALREADY_EXISTS_MESSAGE, result.getResultMessage());
    }

    @Test
    @Order(4)
    @DisplayName("Trying to log in with incorrect credentials")
    void userIncorrectLoginTest() throws Exception {
        OperationResult result = callAsync(
            MessageType.LOGIN_USER,
            // wrong password, right username
            getLoginJson(username, "password12!"));

        short UNAUTHORIZED_HTTP_CODE = HttpStatusCodes.UNAUTHORIZED;
        assertEquals(UNAUTHORIZED_HTTP_CODE, result.getStatusCode());
        String UNAUTHORIZED_MESSAGE = "Unauthorized";
        assertEquals(UNAUTHORIZED_MESSAGE, result.getResultMessage());

        result = callAsync(
            MessageType.LOGIN_USER,
            // wrong username, right password
            getLoginJson("crypto-" + username, clearPassword));
        assertEquals(UNAUTHORIZED_HTTP_CODE, result.getStatusCode());
        assertEquals(UNAUTHORIZED_MESSAGE, result.getResultMessage());
    }

    @Test
    @Order(5)
    @DisplayName("Test the correct login process")
    void userCorrectLoginTest() throws Exception {
        UserOperationResult result = (UserOperationResult) callAsync(
            MessageType.LOGIN_USER,
            getLoginJson(username, clearPassword));

        assertEquals(HttpStatusCodes.OK, result.getStatusCode());
        // check if the result contains both the Player object and the accessToken
        Player relatedUser = result.getRelatedUser();
        assertEquals(username, relatedUser.getUsername());
        accessToken = result.getAccessToken();
        assertFalse(accessToken.isBlank());
    }

    @Test
    @Order(6)
    @DisplayName("Test the accessToken refresh process")
    void refreshTokenTest() throws Exception {
        System.out.println(
            "We have to wait for a while before requesting a token refresh. " +
                "Otherwise we are going to obtain the same token as before. " +
                "Vert.x web is responsible for this behaviour, since i use their JWT implementation."
        );
        Thread.sleep(2000);
        Optional<Player> optionalPlayer = userDB.getDocumentByField("username", username);
        optionalPlayer.ifPresentOrElse(player -> {
            try {
                UserOperationResult result = (UserOperationResult) callAsync(
                    MessageType.REFRESH_ACCESS_TOKEN,
                    player.getRefreshToken());
                assertEquals(HttpStatusCodes.OK, result.getStatusCode());
                // check if the returned accessToken is really different from the previous one
                assertNotEquals(accessToken, result.getAccessToken());

                // Otherwise, if i provide a non-valid refreshToken, the process should fail
                OperationResult failedResult = callAsync(
                    MessageType.REFRESH_ACCESS_TOKEN,
                    player.getPassword());
                assertEquals(HttpStatusCodes.FORBIDDEN, failedResult.getStatusCode());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, () -> fail("Username not present in the database. RefreshToken not possible."));
    }

    @Test
    @Order(7)
    @DisplayName("Test logout process")
    void userLogoutTest() throws Exception {
        Bson userQuery = Filters.eq("username", username);
        Optional<Player> optionalPlayer = userDB.getDocumentByQuery(userQuery);
        optionalPlayer.ifPresentOrElse(player -> {
            try {
                OperationResult result = callAsync(
                    MessageType.LOGOUT_USER,
                    player.getRefreshToken());
                assertEquals(HttpStatusCodes.OK, result.getStatusCode());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, () -> fail("The user is not present in the database. Logout process not possible."));

        // check if the user was really logged out by verifying
        // that his refreshToken was removed from the database
        Optional<Player> optionalLoggedOutPlayer = userDB.getDocumentByQuery(userQuery);
        optionalLoggedOutPlayer.ifPresentOrElse(
            loggedOutPlayer -> assertNull(loggedOutPlayer.getRefreshToken()),
            () -> fail("The logged out user is not present in the database."));
    }

    @Test
    @Order(8)
    @DisplayName("Test user deletion process")
    void deleteUserTest() throws Exception {
        // First of all we need to log back in
        UserOperationResult loginResult = (UserOperationResult) callAsync(
            MessageType.LOGIN_USER,
            getLoginJson(username, clearPassword));

        assertEquals(HttpStatusCodes.OK, loginResult.getStatusCode());

        // then, we test the deletion process
        Player relatedUser = loginResult.getRelatedUser();
        OperationResult result = callAsync(
            MessageType.DELETE_USER,
            relatedUser.getUsername());
        assertEquals(HttpStatusCodes.OK, result.getStatusCode());

        // make sure that the user in the database reflects the changes
        Optional<Player> optionalPlayer = userDB.getDocumentByField("username", relatedUser.getUsername());
        optionalPlayer.ifPresentOrElse(player -> {
            assertTrue(player.isDisabled());
            assertNull(player.getEmail());
        }, () -> fail("The user " + relatedUser.getUsername() + " was not found in the database"));

        // after the deletion process, we shouldn't be able to log the user in again
        OperationResult loginErrorResult = callAsync(
            MessageType.LOGIN_USER,
            getLoginJson(username, clearPassword));
        assertEquals(HttpStatusCodes.UNAUTHORIZED, loginErrorResult.getStatusCode());
    }

    private String getRegistrationJson(String username, String email, String clearPassword) {
        return "{\"username\":" + username + "," +
            "\"email\":" + email + "," +
            "\"password\":" + clearPassword + "}";
    }

    private String getLoginJson(String username, String clearPassword) {
        return "{\"username\":" + username + "," +
            "\"password\":" + clearPassword + "}";
    }

    private OperationResult callAsync(MessageType messageType, String requestBody) throws Exception {
        final CompletableFuture<String> result = new CompletableFuture<>();
        executorService.execute(() -> client.call(messageType, requestBody, result::complete));
        return Presentation.deserializeAs(result.get(), UserOperationResult.class);
    }
}

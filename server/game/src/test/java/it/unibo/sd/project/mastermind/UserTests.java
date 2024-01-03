package it.unibo.sd.project.mastermind;

import it.unibo.sd.project.mastermind.model.Player;
import it.unibo.sd.project.mastermind.model.mongo.DBManager;
import it.unibo.sd.project.mastermind.model.user.OperationResult;
import it.unibo.sd.project.mastermind.model.user.UserManager;
import it.unibo.sd.project.mastermind.presentation.Presentation;
import it.unibo.sd.project.mastermind.rabbit.MessageType;
import it.unibo.sd.project.mastermind.rabbit.RPCClient;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserTests {
    private ExecutorService executorService;
    private RPCClient client;
    private List<Player> usersToDelete;
    private DBManager<Player> userDB;
    private String username;
    private String email;
    private String clearPassword;

    @BeforeAll
    public void setUpTests() throws IOException, TimeoutException {
        new UserManager();
        client = new RPCClient();
        usersToDelete = new ArrayList<>();
        userDB = new DBManager<>("huesle-db", "users", "username", Player.class);
        username = "albisyx";
        email = "albisyx@protonmail.ch";
        clearPassword = "passwd123!";
        // Delete the possible user with this username
        userDB.remove(username);
    }

    @BeforeEach
    public void initExecutor() {
        executorService = Executors.newSingleThreadExecutor();
    }

    @AfterEach
    public void stopExecutor(){
        executorService.shutdown();
    }

    @Test
    @Order(1)
    @DisplayName("Test the correct registration process")
    void userRegistrationTest() throws Exception {
        CompletableFuture<String> response = callAsync(
                client,
                MessageType.REGISTER_USER,
                getRegistrationJson(username, email, clearPassword));

        OperationResult result = Presentation.deserializeAs(response.get(), OperationResult.class);
        short REGISTRATION_DONE_HTTP_CODE = 201;
        assertEquals(REGISTRATION_DONE_HTTP_CODE, result.getStatusCode());
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
        CompletableFuture<String> response = callAsync(
                client,
                MessageType.REGISTER_USER,
                getRegistrationJson(username + "1", email, clearPassword));
        OperationResult result = Presentation.deserializeAs(response.get(), OperationResult.class);
        short VALUE_ALREADY_EXISTS_HTTP_CODE = 409;
        assertEquals(VALUE_ALREADY_EXISTS_HTTP_CODE, result.getStatusCode());
        String EMAIL_ALREADY_EXISTS_MESSAGE = "The email address is already in use";
        assertEquals(EMAIL_ALREADY_EXISTS_MESSAGE, result.getResultMessage());

        response = callAsync(
                client,
                MessageType.REGISTER_USER,
                getRegistrationJson(username, "S" + email, clearPassword));
        result = Presentation.deserializeAs(response.get(), OperationResult.class);
        assertEquals(VALUE_ALREADY_EXISTS_HTTP_CODE, result.getStatusCode());
        String USERNAME_ALREADY_EXISTS_MESSAGE = "The username is already in use";
        assertEquals(USERNAME_ALREADY_EXISTS_MESSAGE, result.getResultMessage());
    }

    @Test
    @Order(4)
    @DisplayName("Trying to log in with incorrect credentials")
    void userIncorrectLoginTest() throws Exception {
        CompletableFuture<String> response = callAsync(
                client,
                MessageType.LOGIN_USER,
                // right username but wrong password
                getLoginJson(username, "password12!"));
        OperationResult result = Presentation.deserializeAs(response.get(), OperationResult.class);
        short UNAUTHORIZED_HTTP_CODE = 401;
        assertEquals(UNAUTHORIZED_HTTP_CODE, result.getStatusCode());
        String UNAUTHORIZED_MESSAGE = "Unauthorized";
        assertEquals(UNAUTHORIZED_MESSAGE, result.getResultMessage());

        response = callAsync(
                client,
                MessageType.LOGIN_USER,
                // right password but wrong username
                getLoginJson("crypto", clearPassword));
        result = Presentation.deserializeAs(response.get(), OperationResult.class);
        assertEquals(UNAUTHORIZED_HTTP_CODE, result.getStatusCode());
        assertEquals(UNAUTHORIZED_MESSAGE, result.getResultMessage());
    }

    @Test
    @Order(5)
    @DisplayName("Test the correct login process")
    void userCorrectLoginTest() throws Exception {
        CompletableFuture<String> response = callAsync(
                client,
                MessageType.LOGIN_USER,
                getLoginJson(username, clearPassword));
        OperationResult result = Presentation.deserializeAs(response.get(), OperationResult.class);
        short SUCCESS_HTTP_CODE = 200;
        assertEquals(SUCCESS_HTTP_CODE, result.getStatusCode());
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

    private CompletableFuture<String> callAsync(RPCClient client, MessageType messageType, String requestBody) {
        final CompletableFuture<String> result = new CompletableFuture<>();
        executorService.execute(() -> client.call(messageType, requestBody, result::complete));
        return result;
    }

    @Test
    void passwordVerification() {
        String email = "mario.rossi@unibo.it";
        String username = "mariorossi";
        String clearPassword = "Mario123!";
        Player player = new Player(username, email, clearPassword);

        assertTrue(player.verifyPassword("Mario123!"), "The given password doesn't match with the user password");
    }
}

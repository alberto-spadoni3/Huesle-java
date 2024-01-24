package it.unibo.sd.project.mastermind;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mongodb.client.MongoDatabase;
import it.unibo.sd.project.mastermind.controllers.UserController;
import it.unibo.sd.project.mastermind.model.user.AccessibilitySettings;
import it.unibo.sd.project.mastermind.model.GameManager;
import it.unibo.sd.project.mastermind.model.OperationResult;
import it.unibo.sd.project.mastermind.model.user.Player;
import it.unibo.sd.project.mastermind.model.mongo.DBManager;
import it.unibo.sd.project.mastermind.model.mongo.DBSingleton;
import it.unibo.sd.project.mastermind.model.user.UserOperationResult;
import it.unibo.sd.project.mastermind.presentation.Presentation;
import it.unibo.sd.project.mastermind.rabbit.MessageType;
import it.unibo.sd.project.mastermind.rabbit.RPCClient;
import org.junit.jupiter.api.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SettingsOperationsTests {
    private ExecutorService executorService;
    private RPCClient client;
    private DBManager<Player> userDB;
    private Player player1;

    @BeforeAll
    public void setUpTests() throws Exception {
        new GameManager(true);
        client = new RPCClient();
        MongoDatabase testDatabase = DBSingleton.getInstance().getTestDatabase();
        // Drop the possible existing database to avoid conflicts
        testDatabase.drop();

        this.userDB = new DBManager<>(testDatabase, "users", "username", Player.class);

        // register at least three users so that two matches can be created
        registerUser(testDatabase);
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
    void getSettingsTest() throws Exception {
        CompletableFuture<String> response = callAsync(MessageType.GET_SETTINGS, "Alice");
        OperationResult error = Presentation.deserializeAs(response.get(), UserOperationResult.class);
        // since we provided a username that doesn't exists in the database,
        // the request should have returned with an error
        assertEquals(400, error.getStatusCode());
        System.out.println(error.getResultMessage());

        response = callAsync(MessageType.GET_SETTINGS, player1.getUsername());
        UserOperationResult result = Presentation.deserializeAs(response.get(), UserOperationResult.class);
        assertEquals(200, result.getStatusCode());

        Player relatedUser = result.getRelatedUser();
        assertEquals(player1.getProfilePictureID(), relatedUser.getProfilePictureID());
        assertEquals(player1.getSettings(), result.getRelatedUser().getSettings());
        System.out.println(result.getResultMessage());
    }

    @Test
    void updateProfilePictureTest() throws Exception {
        byte newProfilePic = (byte) 4;
        JsonPrimitive profilePicPrimitive = new JsonPrimitive(newProfilePic);
        String requestBody = getRequest("Alice", "profilePictureID", profilePicPrimitive);
        CompletableFuture<String> response = callAsync(MessageType.UPDATE_PROFILE_PIC, requestBody);
        OperationResult error = Presentation.deserializeAs(response.get(), UserOperationResult.class);
        // since we provided a username that doesn't exists in the database,
        // the request should have returned with an error
        assertEquals(400, error.getStatusCode());
        System.out.println(error.getResultMessage());

        requestBody = getRequest(player1.getUsername(), "profilePictureID", profilePicPrimitive);
        response = callAsync(MessageType.UPDATE_PROFILE_PIC, requestBody);
        OperationResult result = Presentation.deserializeAs(response.get(), UserOperationResult.class);
        assertEquals(200, result.getStatusCode());
        // now we should see some changes in the DB
        Player updatedPlayer = userDB
                .getDocumentByField("username", player1.getUsername())
                .orElseThrow();
        assertEquals(newProfilePic, updatedPlayer.getProfilePictureID());
        System.out.println(result.getResultMessage());
    }

    @Test
    void updateAccessibilitySettingsTest() throws Exception {
        AccessibilitySettings newSettings = new AccessibilitySettings(true, true);
        JsonElement jsonSettings = Presentation.serializerOf(AccessibilitySettings.class).getJsonElement(newSettings);
        String request = getRequest("Alice", "accessibilitySettings", jsonSettings);
        CompletableFuture<String> response = callAsync(MessageType.UPDATE_SETTINGS, request);
        OperationResult error = Presentation.deserializeAs(response.get(), UserOperationResult.class);
        // since we provided a username that doesn't exists in the database, the request should
        // have returned with an error
        assertEquals(400, error.getStatusCode());
        System.out.println(error.getResultMessage());

        request = getRequest(player1.getUsername(), "accessibilitySettings", jsonSettings);
        response = callAsync(MessageType.UPDATE_SETTINGS, request);
        OperationResult result = Presentation.deserializeAs(response.get(), UserOperationResult.class);
        assertEquals(200, result.getStatusCode());
        // now we should see some changes in the DB
        Player updatedPlayer = userDB
                .getDocumentByField("username", player1.getUsername())
                .orElseThrow();
        assertEquals(newSettings, updatedPlayer.getSettings());
        System.out.println(result.getResultMessage());
    }

    private String getRequest(String requesterUsername, String dataKey, JsonElement dataValue) {
        JsonObject result = new JsonObject();
        result.addProperty("requesterUsername", requesterUsername);
        result.add(dataKey, dataValue);
        return result.toString();
    }

    private CompletableFuture<String> callAsync(MessageType messageType, String requestBody) {
        final CompletableFuture<String> result = new CompletableFuture<>();
        executorService.execute(() -> client.call(messageType, requestBody, result::complete));
        return result;
    }

    private void registerUser(MongoDatabase database) throws Exception {
        UserController userController = new UserController(database);
        player1 = new Player("bob", "bob@huesle.it", "password");
        String registrationResponse = userController
                .registerUser()
                .apply(Presentation.serializerOf(Player.class).serialize(player1));
        OperationResult opRes = Presentation.deserializeAs(registrationResponse, UserOperationResult.class);
        if (opRes.getStatusCode() >= 400)
            throw new RuntimeException("Preliminary user registration had some problems...");
    }
}

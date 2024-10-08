package it.unibo.sd.project.mastermind;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import it.unibo.sd.project.mastermind.controllers.UserController;
import it.unibo.sd.project.mastermind.controllers.utils.HttpStatusCodes;
import it.unibo.sd.project.mastermind.model.match.Hints;
import it.unibo.sd.project.mastermind.model.match.Match;
import it.unibo.sd.project.mastermind.model.match.MatchState;
import it.unibo.sd.project.mastermind.model.mongo.DBManager;
import it.unibo.sd.project.mastermind.model.mongo.DBSingleton;
import it.unibo.sd.project.mastermind.model.request.PendingMatchRequest;
import it.unibo.sd.project.mastermind.model.result.GuessOperationResult;
import it.unibo.sd.project.mastermind.model.result.MatchOperationResult;
import it.unibo.sd.project.mastermind.model.result.OperationResult;
import it.unibo.sd.project.mastermind.model.result.UserOperationResult;
import it.unibo.sd.project.mastermind.model.user.Player;
import it.unibo.sd.project.mastermind.presentation.Presentation;
import it.unibo.sd.project.mastermind.rabbit.MessageType;
import it.unibo.sd.project.mastermind.rabbit.RPCClient;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GameOperationsTests {
    private ExecutorService executorService;
    private RPCClient client;
    private DBManager<Match> matchDB;
    private DBManager<PendingMatchRequest> pendingRequestDB;
    private Player player1;
    private Player player2;
    private Player player3;
    private String matchAccessCode;
    private List<Match> matchesOfPlayer2;

    @BeforeAll
    public void setUpTests() throws Exception {
        new GameManager(true);
        client = new RPCClient();
        MongoDatabase testDatabase = DBSingleton.getTestDatabase();
        // Drop the possible existing database to avoid conflicts
        testDatabase.drop();

        this.matchDB = new DBManager<>(testDatabase, "matches", "_id", Match.class);
        this.pendingRequestDB = new DBManager<>(testDatabase, "pendingRequests", "requesterUsername",
            PendingMatchRequest.class);

        // register at least three users so that two matches can be created
        registerUsers(testDatabase);
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
    void initiallyEmpty() throws Exception {
        CompletableFuture<String> response = callAsync(
            client,
            MessageType.GET_MATCHES_OF_USER,
            player1.getUsername());

        MatchOperationResult operationResult = Presentation.deserializeAs(response.get(), MatchOperationResult.class);
        assertEquals(HttpStatusCodes.OK, operationResult.getStatusCode());
        System.out.println(operationResult.getResultMessage());
        assertEquals(0, operationResult.getMatches().size());
    }

    @Test
    @Order(2)
    void searchPublicMatch() throws Exception {
        String request = getRequestForMatch(player1, false);
        CompletableFuture<String> response = callAsync(
            client,
            MessageType.SEARCH_MATCH,
            request);

        OperationResult operationResult = Presentation.deserializeAs(response.get(), MatchOperationResult.class);
        // the searching process should succeed with statusCode HttpStatusCodes.OK, meaning that a pending request has been added
        assertEquals(HttpStatusCodes.OK, operationResult.getStatusCode());
        System.out.println(operationResult.getResultMessage());

        // check if there is the supposed pending request
        Bson pendingReqQuery = Filters.and(
            Filters.eq("requesterUsername", player1.getUsername()),
            Filters.eq("matchAccessCode", null)
        );

        Optional<PendingMatchRequest> optionalPendingMatchRequest =
            pendingRequestDB.getDocumentByQuery(pendingReqQuery);
        optionalPendingMatchRequest.ifPresentOrElse(pendingReq -> {
            assertEquals(player1.getUsername(), pendingReq.getRequesterUsername());

            // the current pending request is for a public match: so there's no match access code
            assertNull(pendingReq.getMatchAccessCode());
        }, () -> fail("Pending request of " + player1.getUsername() + " not present in the database"));
    }

    @Test
    @Order(3)
    void duplicatePublicMatchRequest() throws Exception {
        CompletableFuture<String> response = callAsync(
            client,
            MessageType.SEARCH_MATCH,
            getRequestForMatch(player1, false));

        OperationResult operationResult = Presentation.deserializeAs(response.get(), MatchOperationResult.class);
        // the searching process should fail with statusCode HttpStatusCodes.BAD_REQUEST, meaning that a pending request of that user already exists
        assertEquals(HttpStatusCodes.BAD_REQUEST, operationResult.getStatusCode());
        System.out.println(operationResult.getResultMessage());
    }

    @Test
    @Order(4)
    void createPublicMatch() throws Exception {
        CompletableFuture<String> response = callAsync(
            client,
            MessageType.SEARCH_MATCH,
            getRequestForMatch(player2, false));

        MatchOperationResult operationResult = Presentation.deserializeAs(response.get(), MatchOperationResult.class);
        // the result should indicates that a public match between player1 and player2 has been created
        assertEquals(HttpStatusCodes.CREATED, operationResult.getStatusCode());
        System.out.println(operationResult.getResultMessage());

        assertNotNull(operationResult.getMatches().getFirst());
        // check if there is a match in the database
        checkMatchOfPlayerInDB(player2);
    }

    @Test
    @Order(5)
    void searchPrivateMatch() throws Exception {
        CompletableFuture<String> response = callAsync(
            client,
            MessageType.SEARCH_MATCH,
            getRequestForMatch(player2, true));

        MatchOperationResult operationResult = Presentation.deserializeAs(response.get(), MatchOperationResult.class);
        assertEquals(HttpStatusCodes.OK, operationResult.getStatusCode());
        System.out.println(operationResult.getResultMessage());
        matchAccessCode = operationResult.getMatchAccessCode();
        assertNotNull(matchAccessCode);

        // check if there is a pending request in the database
        Bson pendingReqQuery = Filters.and(
            Filters.eq("requesterUsername", player2.getUsername()),
            Filters.ne("matchAccessCode", null)
        );

        Optional<PendingMatchRequest> optionalMatch = pendingRequestDB.getDocumentByQuery(pendingReqQuery);
        optionalMatch.ifPresentOrElse(
            pendingReq -> assertEquals(matchAccessCode, pendingReq.getMatchAccessCode()),
            () -> fail("Pending request with matchAccessCode " + matchAccessCode + " not present in the database"));
    }

    @Test
    @Order(6)
    void duplicatePrivateMatchRequest() throws Exception {
        CompletableFuture<String> response = callAsync(
            client,
            MessageType.SEARCH_MATCH,
            getRequestForMatch(player2, true));

        OperationResult operationResult = Presentation.deserializeAs(response.get(), MatchOperationResult.class);
        // the searching process should fail with statusCode HttpStatusCodes.BAD_REQUEST, meaning that a pending request of that user already exists
        assertEquals(HttpStatusCodes.BAD_REQUEST, operationResult.getStatusCode());
        System.out.println(operationResult.getResultMessage());
    }

    @Test
    @Order(7)
    void joinPrivateMatchWithIncorrectCode() throws Exception {
        CompletableFuture<String> response = callAsync(
            client,
            MessageType.JOIN_PRIVATE_MATCH,
            getRequestForMatch(player3, matchAccessCode + 1));

        OperationResult operationResult = Presentation.deserializeAs(response.get(), MatchOperationResult.class);
        // the searching process should fail with statusCode HttpStatusCodes.BAD_REQUEST, meaning that a pending request already exists
        assertEquals(HttpStatusCodes.NOT_FOUND, operationResult.getStatusCode());
        System.out.println(operationResult.getResultMessage());
    }

    @Test
    @Order(8)
    void joinPrivateMatchWithCorrectCode() throws Exception {
        CompletableFuture<String> response = callAsync(
            client,
            MessageType.JOIN_PRIVATE_MATCH,
            getRequestForMatch(player3, matchAccessCode));

        MatchOperationResult operationResult = Presentation.deserializeAs(response.get(), MatchOperationResult.class);
        // the result should indicate that a public match between player2 and player3 has been created
        assertEquals(HttpStatusCodes.CREATED, operationResult.getStatusCode());
        System.out.println(operationResult.getResultMessage());

        assertNotNull(operationResult.getMatches().getFirst());
        // check if there is a match in the database
        checkMatchOfPlayerInDB(player3);
    }

    @Test
    @Order(9)
    void getAllMatchesOfPlayer() throws Exception {
        CompletableFuture<String> response = callAsync(
            client,
            MessageType.GET_MATCHES_OF_USER,
            player2.getUsername());

        MatchOperationResult operationResult = Presentation.deserializeAs(response.get(), MatchOperationResult.class);
        assertEquals(HttpStatusCodes.OK, operationResult.getStatusCode());
        System.out.println(operationResult.getResultMessage());

        // saving matches list in a class field in order to use it in the next tests
        matchesOfPlayer2 = operationResult.getMatches();

        // since player2 was involved in the creation of two matches, the response should contain them all
        assertEquals(2, matchesOfPlayer2.size());
        AtomicBoolean isPlayerInAllMatches = new AtomicBoolean(true);
        matchesOfPlayer2.forEach(match -> {
            if (!match.getMatchStatus().getPlayers().contains(player2))
                isPlayerInAllMatches.set(false);
        });
        assertTrue(isPlayerInAllMatches.get());
    }

    @Test
    @Order(9)
    void doGuess() throws Exception {
        // Let's first try to do a guess NOT made from the current next player
        JsonObject faultyRequest = getRequestForGuess(true);

        CompletableFuture<String> failedResponse = callAsync(
            client,
            MessageType.DO_GUESS,
            faultyRequest.toString());

        OperationResult operationResult = Presentation.deserializeAs(failedResponse.get(), GuessOperationResult.class);
        // the request should fail and the guess should not be submitted
        assertEquals(HttpStatusCodes.BAD_REQUEST, operationResult.getStatusCode());
        System.out.println(operationResult.getResultMessage());

        // Now let's try again with a correct request
        JsonObject correctRequest = getRequestForGuess(false);
        CompletableFuture<String> successfulResponse = callAsync(
            client,
            MessageType.DO_GUESS,
            correctRequest.toString());

        GuessOperationResult guessOperationResult = Presentation.deserializeAs(successfulResponse.get(),
            GuessOperationResult.class);
        // the request should fail and the guess should not be submitted
        assertEquals(HttpStatusCodes.OK, guessOperationResult.getStatusCode());
        System.out.println(guessOperationResult.getResultMessage());
        // check if the result contains the computed hints
        Hints submittedAttemptHints = guessOperationResult.getSubmittedAttemptHints();
        assertNotNull(submittedAttemptHints);
        System.out.printf("rightPositions: %d, rightColours: %d%n", submittedAttemptHints.getRightPositions(),
            submittedAttemptHints.getRightColours());
    }

    @Test
    @Order(10)
    void getMatchByID() throws Exception {
        String matchID = matchesOfPlayer2.getFirst().getMatchID().toString();
        CompletableFuture<String> response = callAsync(
            client,
            MessageType.GET_MATCH,
            matchID);

        MatchOperationResult operationResult = Presentation.deserializeAs(response.get(), MatchOperationResult.class);
        assertEquals(HttpStatusCodes.OK, operationResult.getStatusCode());
        System.out.println(operationResult.getResultMessage());
        assertEquals(1, operationResult.getMatches().size());
        assertEquals(matchID, operationResult.getMatches().getFirst().getMatchID().toString());
    }

    @Test
    @Order(11)
    void leaveMatchByID() throws Exception {
        String matchID = matchesOfPlayer2.get(1).getMatchID().toString();
        JsonObject request = new JsonObject();
        request.addProperty("requesterUsername", player2.getUsername());
        request.addProperty("matchID", matchID);
        CompletableFuture<String> response = callAsync(
            client,
            MessageType.LEAVE_MATCH,
            request.toString());

        OperationResult operationResult = Presentation.deserializeAs(response.get(), MatchOperationResult.class);
        assertEquals(HttpStatusCodes.OK, operationResult.getStatusCode());
        System.out.println(operationResult.getResultMessage());

        // check if the leaved match is marked as abandoned
        Optional<Match> optionalMatch = matchDB.getDocumentByQuery(Filters.eq(matchID));
        optionalMatch.ifPresentOrElse(leavedMatch -> {
            assertEquals(matchID, leavedMatch.getMatchID().toString());
            assertEquals(MatchState.VICTORY, leavedMatch.getMatchStatus().getState());
            assertNotEquals(leavedMatch.getMatchStatus().getNextPlayer(), player2.getUsername());
            assertTrue(leavedMatch.getMatchStatus().isAbandoned());
        }, () -> fail("The leaved match with ID " + matchID + " was not found in the database."));
    }


    // PRIVATE METHODS
    private CompletableFuture<String> callAsync(RPCClient client, MessageType messageType, String requestBody) {
        final CompletableFuture<String> result = new CompletableFuture<>();
        executorService.execute(() -> client.call(messageType, requestBody, result::complete));
        return result;
    }

    private void checkMatchOfPlayerInDB(Player player) throws Exception {
        Bson matchQuery = Filters.elemMatch("matchStatus.players", Filters.eq("username", player.getUsername()));
        Optional<Match> optionalMatch = matchDB.getDocumentByQuery(matchQuery);
        optionalMatch.ifPresentOrElse(match -> {
            assertTrue(match.getMatchStatus().getPlayers().contains(player));
            assertEquals(0, match.getMadeAttempts().size());
            assertEquals(MatchState.PLAYING, match.getMatchStatus().getState());
        }, () -> fail("Match not present in the database"));
    }

    private void registerUsers(MongoDatabase database) throws Exception {
        UserController userController = new UserController(database);
        player1 = new Player("bob", "bob@huesle.it", "password");
        player2 = new Player("alice", "alice@huesle.it", "password");
        player3 = new Player("sara", "sara@huesle.it", "password");
        for (Player player : List.of(player1, player2, player3)) {
            String registrationResponse = userController.registerUser()
                .apply(Presentation.serializerOf(Player.class).serialize(player));
            OperationResult opRes = Presentation.deserializeAs(registrationResponse, UserOperationResult.class);
            if (opRes.getStatusCode() >= HttpStatusCodes.BAD_REQUEST)
                throw new RuntimeException("Preliminary user registration had some problems...");
        }
    }

    private String getRequestForMatch(Player player, boolean privateMatch) {
        return getRequestForMatch(player, privateMatch, null);
    }

    private String getRequestForMatch(Player player, String matchAccessCode) {
        return getRequestForMatch(player, true, matchAccessCode);
    }

    private String getRequestForMatch(Player player, boolean privateMatch, String matchAccessCode) {
        JsonObject request = new JsonObject();
        request.addProperty("requesterUsername", player.getUsername());
        request.addProperty("isPrivateMatch", privateMatch);
        if (matchAccessCode != null)
            request.addProperty("matchAccessCode", matchAccessCode);
        return request.toString();
    }

    private JsonObject getRequestForGuess(boolean hasToFail) {
        JsonObject request = new JsonObject();

        // the requesterUsername must correspond to the match's next player
        Match match = matchesOfPlayer2.getFirst();
        String nextPlayer = match.getMatchStatus().getNextPlayer();
        // if the request has to fail, the requester must not correspond to the match's next player
        String requesterUsername = hasToFail ?
            (player2.getUsername().equals(nextPlayer) ? player1.getUsername() : player2.getUsername()) :
            (player2.getUsername().equals(nextPlayer) ? player2.getUsername() : player1.getUsername());
        request.addProperty("requesterUsername", requesterUsername);

        String matchID = match.getMatchID().toString();
        request.addProperty("matchID", matchID);

        JsonArray colorSequence = new JsonArray();
        for (String color : List.of("crimson", "gold", "coral", "coral"))
            colorSequence.add(color);
        request.add("colorSequence", colorSequence);
        return request;
    }
}

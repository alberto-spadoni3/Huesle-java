package it.unibo.sd.project.mastermind.controllers;

import com.mongodb.client.MongoDatabase;
import it.unibo.sd.project.mastermind.model.Player;
import it.unibo.sd.project.mastermind.model.match.Match;
import it.unibo.sd.project.mastermind.model.match.MatchOperationResult;
import it.unibo.sd.project.mastermind.model.match.PendingMatchRequest;
import it.unibo.sd.project.mastermind.model.match.SearchRequest;
import it.unibo.sd.project.mastermind.model.mongo.DBManager;
import it.unibo.sd.project.mastermind.presentation.Presentation;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Filters.eq;

public class GameController {
    private final DBManager<PendingMatchRequest> pendingMatchDB;
    private final DBManager<Match> matchDB;
    private final DBManager<Player> userDB;

    public GameController(MongoDatabase database) {
        pendingMatchDB = new DBManager<>(database, "pendingRequests", "requesterUsername", PendingMatchRequest.class);
        userDB = new DBManager<>(database, "users", "username", Player.class);
        matchDB = new DBManager<>(database, "matches", "matchID", Match.class);
    }

    public Function<String, String> searchMatch() {
        return message -> {
            MatchOperationResult matchOperationResult = null;
            try {
                SearchRequest request = Presentation.deserializeAs(message, SearchRequest.class);
                String requesterUsername = request.getRequester();

                // check if there is a pendingRequest made by the current user
                Bson pendingReqOfCurrentPlayer =
                        and(
                                eq("requesterUsername", requesterUsername),
                                request.isMatchPrivate() ?
                                        ne("matchAccessCode", null) :
                                        eq("matchAccessCode", null));
                Optional<PendingMatchRequest> optionalPending = pendingMatchDB.getDocumentByQuery(pendingReqOfCurrentPlayer);

                if (optionalPending.isEmpty()) {
                    // there aren't any pending request made by the current user.
                    // let's check if there is one made by another player
                    if (!request.isMatchPrivate()) {
                        Bson pendingReqOfAnotherPlayer =
                                and(
                                        ne("requesterUsername", requesterUsername),
                                        eq("matchAccessCode", null));
                        Optional<PendingMatchRequest> possiblePublicMatch = pendingMatchDB.getDocumentByQuery(pendingReqOfAnotherPlayer);
                        if (possiblePublicMatch.isPresent()) {
                            // pending request found: we can now create a public match
                            createMatch(requesterUsername, possiblePublicMatch.get().getRequesterUsername());
                            // delete the pending request just fulfilled
                            pendingMatchDB.deleteByQuery(pendingReqOfAnotherPlayer);

                            matchOperationResult =
                                    new MatchOperationResult((short) 201, "Public match created");
                            return Presentation.serializerOf(MatchOperationResult.class).serialize(matchOperationResult);
                        }
                    }

                    // pending request not found: let's create one
                    // here there's no need to distinguish between a private or public match
                    PendingMatchRequest pendingMatchRequest = new PendingMatchRequest(requesterUsername);
                    if (request.isMatchPrivate())
                        pendingMatchRequest.setMatchAccessCode(generateMatchAccessCode());

                    // save the new pending request in the database
                    pendingMatchDB.insert(pendingMatchRequest);

                    matchOperationResult =
                            new MatchOperationResult(
                                    (short) 200,
                                    "Searching another player",
                                    pendingMatchRequest.getMatchAccessCode());
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                if (matchOperationResult == null) // a pending request already exists
                    matchOperationResult = new MatchOperationResult((short) 400, "User already pending for a match");
            }
            return Presentation.serializerOf(MatchOperationResult.class).serialize(matchOperationResult);
        };
    }

    public Function<String, String> joinPrivateMatch() {
        return message -> {
            MatchOperationResult matchOperationResult = null;
            try {
                SearchRequest request = Presentation.deserializeAs(message, SearchRequest.class);
                String requesterUsername = request.getRequester();

                // check if there is a pending request with the same access code
                Bson pendingReqWithSpecificCode =
                        and(
                                ne("requesterUsername", requesterUsername),
                                eq("matchAccessCode", request.getMatchAccessCode()));
                Optional<PendingMatchRequest> possiblePrivateMatch = pendingMatchDB.getDocumentByQuery(pendingReqWithSpecificCode);
                if (possiblePrivateMatch.isPresent()) {
                    createMatch(requesterUsername, possiblePrivateMatch.get().getRequesterUsername());
                    pendingMatchDB.deleteByQuery(pendingReqWithSpecificCode);
                    matchOperationResult =
                            new MatchOperationResult((short) 201, "Private match created");
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } finally {
                if (matchOperationResult == null) // no pending request found with that access code
                    matchOperationResult =
                            new MatchOperationResult((short) 404, "No match found with that access code");
            }
            return Presentation.serializerOf(MatchOperationResult.class).serialize(matchOperationResult);
        };
    }

    public Function<String, String> getMatchesOfUser() {
        return username -> {
            try {
                // check if there are matches the current user is involved in
                Bson matchesOfUserQuery = elemMatch("players", in("players", username));
                AtomicReference<List<Match>> matchesOfUser = new AtomicReference<>();
                Optional<List<Match>> optionalMatches = matchDB.getDocumentsByQuery(matchesOfUserQuery);
                optionalMatches.ifPresentOrElse(matchesOfUser::set, () -> matchesOfUser.set(new ArrayList<>()));

                // check if there is a pending request the user has created
                Bson pendingReqOfCurrentPlayer = eq("requesterUsername", username);
                AtomicBoolean pendingRePresent = new AtomicBoolean(false);
                Optional<PendingMatchRequest> optionalPendingReq = pendingMatchDB.getDocumentByQuery(pendingReqOfCurrentPlayer);
                optionalPendingReq.ifPresent(pendingReq -> pendingRePresent.set(true));

                // compose the results
                MatchOperationResult matchOperationResult =
                        new MatchOperationResult(
                                (short) 200,
                                "Returning " + matchesOfUser.get().size() + " matches",
                                matchesOfUser.get(), pendingRePresent.get());
                return Presentation.serializerOf(MatchOperationResult.class).serialize(matchOperationResult);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    private void createMatch(String user1, String user2) throws Exception {
        // get the Player object in the DB from the username
        Player player1 = getPlayer(user1);
        Player player2 = getPlayer(user2);
        List<Player> players = List.of(player1, player2);
        Match newMatch = new Match(players);
        matchDB.insert(newMatch);
    }

    private Player getPlayer(String username) throws Exception {
        Optional<Player> optionalPlayer = userDB.getDocumentByField("username", username);
        return optionalPlayer.orElse(null);
    }

    private String generateMatchAccessCode() throws Exception {
        Optional<PendingMatchRequest> pendingReqWithDuplicateCode;
        StringBuilder secretCode;
        do {
            secretCode = new StringBuilder(String.valueOf(new Random().nextInt(100000)));
            pendingReqWithDuplicateCode = pendingMatchDB.getDocumentByQuery(eq("matchAccessCode", secretCode.toString()));
        } while (pendingReqWithDuplicateCode.isPresent());

        while (secretCode.length() < 5) {
            secretCode.insert(0, "0");
        }
        return secretCode.toString();
    }
}

package it.unibo.sd.project.mastermind.controllers;

import com.mongodb.client.MongoDatabase;
import it.unibo.sd.project.mastermind.controllers.utils.HttpStatusCodes;
import it.unibo.sd.project.mastermind.model.match.Attempt;
import it.unibo.sd.project.mastermind.model.match.Match;
import it.unibo.sd.project.mastermind.model.match.MatchState;
import it.unibo.sd.project.mastermind.model.match.MatchStatus;
import it.unibo.sd.project.mastermind.model.mongo.DBManager;
import it.unibo.sd.project.mastermind.model.request.GuessRequest;
import it.unibo.sd.project.mastermind.model.request.MatchRequest;
import it.unibo.sd.project.mastermind.model.request.PendingMatchRequest;
import it.unibo.sd.project.mastermind.model.result.GuessOperationResult;
import it.unibo.sd.project.mastermind.model.result.MatchOperationResult;
import it.unibo.sd.project.mastermind.model.user.Player;
import it.unibo.sd.project.mastermind.presentation.Presentation;
import org.bson.conversions.Bson;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.mongodb.client.model.Filters.*;

public class GameLogicController {
    private final DBManager<PendingMatchRequest> pendingMatchDB;
    private final DBManager<Match> matchDB;
    private final DBManager<Player> userDB;

    public GameLogicController(MongoDatabase database) {
        pendingMatchDB = new DBManager<>(database, "pendingRequests", "requesterUsername", PendingMatchRequest.class);
        userDB = new DBManager<>(database, "users", "username", Player.class);
        matchDB = new DBManager<>(database, "matches", "_id", Match.class);
    }

    public Function<String, String> searchMatch() {
        return message -> {
            MatchOperationResult matchOperationResult = null;
            try {
                MatchRequest request = Presentation.deserializeAs(message, MatchRequest.class);
                String requesterUsername = request.getRequesterUsername();

                // check if there is a pendingRequest made by the current user
                Bson pendingReqOfCurrentPlayer =
                    and(
                        eq("requesterUsername", requesterUsername),
                        request.isMatchPrivate() ?
                            ne("matchAccessCode", null) :
                            eq("matchAccessCode", null));
                Optional<PendingMatchRequest> optionalPending = pendingMatchDB.getDocumentByQuery(
                    pendingReqOfCurrentPlayer);

                if (optionalPending.isEmpty()) {
                    // there aren't any pending request made by the current user.
                    // let's check if there is one made by another player
                    if (!request.isMatchPrivate()) {
                        Bson pendingReqOfAnotherPlayer =
                            and(
                                ne("requesterUsername", requesterUsername),
                                eq("matchAccessCode", null));
                        Optional<PendingMatchRequest> possiblePublicMatch = pendingMatchDB.getDocumentByQuery(
                            pendingReqOfAnotherPlayer);
                        if (possiblePublicMatch.isPresent()) {
                            // pending request found: we can now create a public match
                            Match newMatch = createMatch(requesterUsername,
                                possiblePublicMatch.get().getRequesterUsername());
                            // delete the pending request just fulfilled
                            pendingMatchDB.deleteByQuery(pendingReqOfAnotherPlayer);

                            matchOperationResult =
                                new MatchOperationResult(HttpStatusCodes.CREATED, "Public match created");
                            matchOperationResult.setMatches(List.of(newMatch));
                            return Presentation.serializerOf(MatchOperationResult.class)
                                .serialize(matchOperationResult);
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
                            HttpStatusCodes.OK,
                            "Searching another player...",
                            pendingMatchRequest.getMatchAccessCode());
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                if (matchOperationResult == null) // a pending request already exists
                    matchOperationResult = new MatchOperationResult(HttpStatusCodes.BAD_REQUEST,
                        "User already pending for a match");
            }
            return Presentation.serializerOf(MatchOperationResult.class).serialize(matchOperationResult);
        };
    }

    public Function<String, String> joinPrivateMatch() {
        return message -> {
            MatchOperationResult matchOperationResult = null;
            try {
                MatchRequest request = Presentation.deserializeAs(message, MatchRequest.class);
                String requesterUsername = request.getRequesterUsername();

                // check if there is a pending request with the same access code
                Bson pendingReqWithSpecificCode =
                    and(
                        ne("requesterUsername", requesterUsername),
                        eq("matchAccessCode", request.getMatchAccessCode()));
                Optional<PendingMatchRequest> possiblePrivateMatch = pendingMatchDB.getDocumentByQuery(
                    pendingReqWithSpecificCode);
                if (possiblePrivateMatch.isPresent()) {
                    Match newMatch = createMatch(requesterUsername, possiblePrivateMatch.get().getRequesterUsername());
                    pendingMatchDB.deleteByQuery(pendingReqWithSpecificCode);
                    matchOperationResult =
                        new MatchOperationResult(HttpStatusCodes.CREATED, "Private match created");
                    matchOperationResult.setMatches(List.of(newMatch));
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } finally {
                if (matchOperationResult == null) // no pending request found with that access code
                    matchOperationResult =
                        new MatchOperationResult(HttpStatusCodes.NOT_FOUND, "No match found with that access code");
            }
            return Presentation.serializerOf(MatchOperationResult.class).serialize(matchOperationResult);
        };
    }

    public Function<String, String> cancelMatchSearch() {
        return username -> {
            AtomicReference<MatchOperationResult> matchOperationResult = new AtomicReference<>();
            try {
                Bson pendingReqOfUserQuery = and(
                    eq("requesterUsername", username),
                    ne("matchAccessCode", null)
                );
                Optional<PendingMatchRequest> optionalPendingReq = pendingMatchDB.getDocumentByQuery(
                    pendingReqOfUserQuery);
                optionalPendingReq.ifPresent(pendingReq -> {
                    pendingMatchDB.deleteByQuery(pendingReqOfUserQuery);
                    matchOperationResult.set(new MatchOperationResult(
                        HttpStatusCodes.OK,
                        "Private match request cancelled."));
                });
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } finally {
                if (matchOperationResult.get() == null)
                    matchOperationResult.set(new MatchOperationResult(
                        HttpStatusCodes.UNAUTHORIZED,
                        "The cancellation request went wrong."));
            }
            return Presentation.serializerOf(MatchOperationResult.class).serialize(matchOperationResult.get());
        };
    }

    public Function<String, String> getMatchesOfUser() {
        return username -> {
            MatchOperationResult matchOperationResult = null;
            try {
                // First of all check if this user is present
                if (userDB.isPresentByID(username)) {
                    // check if there are matches the current user is involved in
                    Bson matchesOfUserQuery = elemMatch("matchStatus.players", eq("username", username));
                    AtomicReference<List<Match>> matchesOfUser = new AtomicReference<>();
                    Optional<List<Match>> optionalMatches = matchDB.getDocumentsByQuery(matchesOfUserQuery);
                    optionalMatches.ifPresentOrElse(
                        matches -> matchesOfUser.set(updateMatches(matches)),
                        () -> matchesOfUser.set(new ArrayList<>()));

                    // check if there is a pending request the user has created
                    Bson pendingReqOfCurrentPlayer = eq("requesterUsername", username);
                    AtomicBoolean pendingReqPresent = new AtomicBoolean(false);
                    Optional<PendingMatchRequest> optionalPendingReq = pendingMatchDB.getDocumentByQuery(
                        pendingReqOfCurrentPlayer);
                    optionalPendingReq.ifPresent(pendingReq -> pendingReqPresent.set(true));

                    // compose the results
                    matchOperationResult =
                        new MatchOperationResult(
                            HttpStatusCodes.OK,
                            "Returning " + matchesOfUser.get().size() + " matches",
                            matchesOfUser.get(), pendingReqPresent.get());
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                if (matchOperationResult == null)
                    matchOperationResult = new MatchOperationResult(
                        HttpStatusCodes.NOT_FOUND, "The player " + username + " was not found.");
            }
            return Presentation.serializerOf(MatchOperationResult.class).serialize(matchOperationResult);
        };
    }

    public Function<String, String> getMatchByID() {
        return matchID -> {
            AtomicReference<MatchOperationResult> matchOperationResult = new AtomicReference<>();
            try {
                Optional<Match> optionalMatch = matchDB.getDocumentByField("_id", matchID);
                optionalMatch.ifPresent(match -> matchOperationResult.set(
                    new MatchOperationResult(
                        HttpStatusCodes.OK,
                        "Returning the match with ID " + matchID,
                        updateMatches(List.of(match)), false)));
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } finally {
                if (matchOperationResult.get() == null)
                    matchOperationResult.set(new MatchOperationResult(
                        HttpStatusCodes.UNAUTHORIZED, "Match not found for the ID " + matchID
                    ));
            }
            return Presentation.serializerOf(MatchOperationResult.class).serialize(matchOperationResult.get());
        };
    }

    public Function<String, String> leaveMatchByID() {
        return message -> {
            MatchOperationResult matchOperationResult = null;
            try {
                MatchRequest matchRequest = Presentation.deserializeAs(message, MatchRequest.class);
                String requesterUsername = matchRequest.getRequesterUsername();
                Bson matchToLeaveQuery = and(
                    eq(matchRequest.getMatchID()),
                    elemMatch("matchStatus.players", eq("username", requesterUsername)));
                Optional<Match> optionalMatch = matchDB.getDocumentByQuery(matchToLeaveQuery);
                if (optionalMatch.isPresent()) {
                    Match matchToLeave = optionalMatch.get();
                    if (matchToLeave.isNotOver()) {
                        MatchStatus matchStatus = matchToLeave.getMatchStatus();
                        matchStatus.setState(MatchState.VICTORY);
                        Stream<Player> otherPlayer = matchToLeave
                            .getMatchStatus()
                            .getPlayers()
                            .stream()
                            .filter(player -> !Objects.equals(player.getUsername(), requesterUsername));
                        otherPlayer.findFirst().ifPresent(p -> matchStatus.changeNextPlayer(p.getUsername()));
                        matchStatus.setAbandoned();
                        matchDB.update(matchRequest.getMatchID(), matchToLeave);
                        matchOperationResult = new MatchOperationResult(
                            HttpStatusCodes.OK,
                            "Match with ID " + matchToLeave.getMatchID() + " left by " + requesterUsername);
                    }
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } finally {
                if (matchOperationResult == null)
                    matchOperationResult = new MatchOperationResult(
                        HttpStatusCodes.UNAUTHORIZED,
                        "Invalid match selected");
            }
            return Presentation.serializerOf(MatchOperationResult.class).serialize(matchOperationResult);
        };
    }

    public Function<String, String> doGuess() {
        return message -> {
            GuessOperationResult guessOperationResult = null;
            try {
                GuessRequest guessRequest = Presentation.deserializeAs(message, GuessRequest.class);
                String requesterUsername = guessRequest.getRequesterUsername();
                Bson matchToPlayQuery = and(
                    eq(guessRequest.getMatchID()),
                    elemMatch("matchStatus.players", eq("username", requesterUsername)));
                Optional<Match> optionalMatch = matchDB.getDocumentByQuery(matchToPlayQuery);
                Attempt attempt = guessRequest.getAttempt();
                if (optionalMatch.isPresent() && optionalMatch.get().isNotOver() && optionalMatch.get()
                    .isPlayerTurn(requesterUsername)) {
                    Match match = optionalMatch.get();
                    match.tryToGuess(attempt);
                    matchDB.update(match.getMatchID().toString(), match);
                    guessOperationResult = new GuessOperationResult(
                        HttpStatusCodes.OK, "Guess made succesfully",
                        match.getMatchStatus(),
                        attempt.getHints());
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } finally {
                if (guessOperationResult == null)
                    guessOperationResult = new GuessOperationResult(HttpStatusCodes.BAD_REQUEST,
                        "Problems with selected match");
            }
            return Presentation.serializerOf(GuessOperationResult.class).serialize(guessOperationResult);
        };
    }

    private Match createMatch(String user1, String user2) throws Exception {
        // get the Player object in the DB from the username
        Player player1 = getPlayer(user1);
        Player player2 = getPlayer(user2);
        List<Player> players = List.of(player1, player2);
        Match newMatch = new Match(players);
        matchDB.insert(newMatch);
        return newMatch;
    }

    private List<Match> updateMatches(List<Match> matches) {
        // before returning found matches, we first need to update
        // the players inside them because they could be changed after the match creation.
        // Finally, we update the related documents in the database
        try {
            for (Match match : matches) {
                List<Player> updatedPlayers = new ArrayList<>(2);
                for (Player player : match.getMatchStatus().getPlayers())
                    updatedPlayers.add(getPlayer(player.getUsername()));
                match.getMatchStatus().updatePlayers(updatedPlayers);
                matchDB.update(match.getMatchID().toString(), match);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return matches;
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
            pendingReqWithDuplicateCode = pendingMatchDB.getDocumentByQuery(
                eq("matchAccessCode", secretCode.toString()));
        } while (pendingReqWithDuplicateCode.isPresent());

        while (secretCode.length() < 5) {
            secretCode.insert(0, "0");
        }
        return secretCode.toString();
    }
}

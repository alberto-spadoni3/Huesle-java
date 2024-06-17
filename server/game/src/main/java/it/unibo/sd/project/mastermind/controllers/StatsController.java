package it.unibo.sd.project.mastermind.controllers;

import com.mongodb.client.MongoDatabase;
import it.unibo.sd.project.mastermind.controllers.utils.HttpStatusCodes;
import it.unibo.sd.project.mastermind.model.match.Match;
import it.unibo.sd.project.mastermind.model.match.MatchState;
import it.unibo.sd.project.mastermind.model.mongo.DBManager;
import it.unibo.sd.project.mastermind.model.result.StatsOperationResult;
import it.unibo.sd.project.mastermind.model.user.Player;
import it.unibo.sd.project.mastermind.presentation.Presentation;
import org.bson.conversions.Bson;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.mongodb.client.model.Filters.*;

public class StatsController {
    private final DBManager<Match> matchDB;
    private final DBManager<Player> userDB;

    public StatsController(MongoDatabase database) {
        matchDB = new DBManager<>(database, "matches", "_id", Match.class);
        userDB = new DBManager<>(database, "users", "username", Player.class);
    }

    public Function<String, String> getUserStats() {
        return username -> {
            StatsOperationResult statsOperationResult = null;
            try {
                if (userDB.isPresentByID(username)) {
                    String nextPlayer = "matchStatus.nextPlayer";
                    String matchState = "matchStatus.matchState";
                    Bson countMatchesWon = and(
                        eq(nextPlayer, username),
                        eq(matchState, MatchState.VICTORY));
                    int matchesWon = getMatchesQuery(username, countMatchesWon);

                    Bson countMatchesLost = and(
                        ne(nextPlayer, username),
                        eq(matchState, MatchState.VICTORY));
                    int matchesLost = getMatchesQuery(username, countMatchesLost);

                    Bson countMatchesDrawn = eq(matchState, MatchState.DRAW);
                    int matchesDrawn = getMatchesQuery(username, countMatchesDrawn);

                    statsOperationResult = new StatsOperationResult(
                        HttpStatusCodes.OK, "Returning user statistics",
                        matchesWon, matchesLost, matchesDrawn
                    );
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } finally {
                if (statsOperationResult == null)
                    statsOperationResult = new StatsOperationResult(
                        HttpStatusCodes.BAD_REQUEST, "Something went wrong. Please retry");
            }
            return Presentation.serializerOf(StatsOperationResult.class).serialize(statsOperationResult);
        };
    }

    private int getMatchesQuery(String username, Bson countCriteria) throws Exception {
        Bson userIsAPlayer = elemMatch("matchStatus.players", eq("username", username));
        Bson matchesQuery = and(
            userIsAPlayer,
            countCriteria
        );
        Optional<List<Match>> matches = matchDB.getDocumentsByQuery(matchesQuery);
        return matches.orElse(List.of()).size();
    }

    /*
    Here, in possible future developments, there will be some other methods for generating
    notifications about events that occur when a player is not online
    */
}

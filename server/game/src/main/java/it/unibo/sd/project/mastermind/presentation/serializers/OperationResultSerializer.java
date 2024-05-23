package it.unibo.sd.project.mastermind.presentation.serializers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unibo.sd.project.mastermind.model.match.Hints;
import it.unibo.sd.project.mastermind.model.match.Match;
import it.unibo.sd.project.mastermind.model.match.MatchStatus;
import it.unibo.sd.project.mastermind.model.result.*;
import it.unibo.sd.project.mastermind.model.user.Player;
import it.unibo.sd.project.mastermind.presentation.Presentation;

import java.util.List;

public class OperationResultSerializer extends AbstractJsonSerializer<OperationResult> {
    @Override
    protected JsonElement toJsonElement(OperationResult object) {
        JsonObject jsonOpResult = new JsonObject();
        jsonOpResult.addProperty("statusCode", object.getStatusCode());
        jsonOpResult.addProperty("resultMessage", object.getResultMessage());
        switch (object) {
            case UserOperationResult userOperationResult -> {
                Player possibleUser = userOperationResult.getRelatedUser();
                String accessToken = userOperationResult.getAccessToken();
                if (possibleUser != null && accessToken != null) {
                    JsonElement jsonPlayer = Presentation.serializerOf(Player.class).getJsonElement(possibleUser);
                    if (jsonPlayer.isJsonObject())
                        jsonOpResult.add("relatedUser", jsonPlayer);
                    jsonOpResult.addProperty("accessToken", accessToken);
                }
            }
            case MatchOperationResult matchOperationResult -> {
                String matchAccessCode = matchOperationResult.getMatchAccessCode();
                List<Match> matches = matchOperationResult.getMatches();
                boolean pendingMatchPresence = matchOperationResult.isPendingMatchPresence();
                if (matchAccessCode != null)
                    jsonOpResult.addProperty("matchAccessCode", matchAccessCode);
                else if (matches != null) {
                    JsonArray jsonMatches = new JsonArray();
                    for (Match match : matches)
                        jsonMatches.add(Presentation.serializerOf(Match.class).getJsonElement(match));
                    jsonOpResult.add("matches", jsonMatches);
                    jsonOpResult.addProperty("pending", pendingMatchPresence);
                }
            }
            case GuessOperationResult guessOperationResult -> {
                if (guessOperationResult.getSubmittedAttemptHints() != null) {
                    MatchStatus updatedState = guessOperationResult.getUpdatedState();
                    JsonElement jsonState = Presentation.serializerOf(MatchStatus.class).getJsonElement(updatedState);
                    jsonOpResult.add("updatedStatus", jsonState);
                    Hints submittedAttemptHints = guessOperationResult.getSubmittedAttemptHints();
                    JsonElement jsonSubmittedAttemptHints = Presentation.serializerOf(Hints.class)
                        .getJsonElement(submittedAttemptHints);
                    jsonOpResult.add("submittedAttemptHints", jsonSubmittedAttemptHints);
                }
            }
            case StatsOperationResult statsOperationResult -> {
                JsonObject userStats = new JsonObject();
                userStats.addProperty("matches_won", statsOperationResult.getMatchesWon());
                userStats.addProperty("matches_lost", statsOperationResult.getMatchesLost());
                userStats.addProperty("matches_drawn", statsOperationResult.getMatchesDrawn());
                jsonOpResult.add("userStats", userStats);
            }
            default -> {}
        }
        return jsonOpResult;
    }
}

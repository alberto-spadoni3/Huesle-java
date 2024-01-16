package it.unibo.sd.project.mastermind.presentation.serializers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unibo.sd.project.mastermind.model.GuessOperationResult;
import it.unibo.sd.project.mastermind.model.OperationResult;
import it.unibo.sd.project.mastermind.model.Player;
import it.unibo.sd.project.mastermind.model.match.Match;
import it.unibo.sd.project.mastermind.model.match.MatchOperationResult;
import it.unibo.sd.project.mastermind.model.user.UserOperationResult;
import it.unibo.sd.project.mastermind.presentation.Presentation;

import java.util.List;

public class OperationResultSerializer extends AbstractJsonSerializer<OperationResult> {
    @Override
    protected JsonElement toJsonElement(OperationResult object) {
        JsonObject jsonOpResult = new JsonObject();
        jsonOpResult.addProperty("statusCode", object.getStatusCode());
        jsonOpResult.addProperty("resultMessage", object.getResultMessage());
        if (object instanceof UserOperationResult userOperationResult) {
            Player possibleUser = userOperationResult.getRelatedUser();
            String accessToken = userOperationResult.getAccessToken();
            if (possibleUser != null && accessToken != null) {
                JsonElement jsonPlayer = Presentation.serializerOf(Player.class).getJsonElement(possibleUser);
                if (jsonPlayer.isJsonObject())
                    jsonOpResult.add("relatedUser", jsonPlayer);
                jsonOpResult.addProperty("accessToken", accessToken);
            }
        } else if (object instanceof MatchOperationResult matchOperationResult) {
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
        } else if (object instanceof GuessOperationResult guessOperationResult) {
            if (guessOperationResult.getPlayedMatch() != null) {
                Match playedMatch = guessOperationResult.getPlayedMatch();
                JsonElement jsonMatch = Presentation.serializerOf(Match.class).getJsonElement(playedMatch);
                jsonOpResult.add("playedMatch", jsonMatch);
            }
        }
        return jsonOpResult;
    }
}

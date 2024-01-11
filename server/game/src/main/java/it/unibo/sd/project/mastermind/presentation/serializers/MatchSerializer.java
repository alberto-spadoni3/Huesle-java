package it.unibo.sd.project.mastermind.presentation.serializers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unibo.sd.project.mastermind.model.Attempt;
import it.unibo.sd.project.mastermind.model.SecretCode;
import it.unibo.sd.project.mastermind.model.match.Match;
import it.unibo.sd.project.mastermind.model.match.MatchStatus;
import it.unibo.sd.project.mastermind.presentation.Presentation;

public class MatchSerializer extends AbstractJsonSerializer<Match>{
    @Override
    protected JsonElement toJsonElement(Match match) {
        JsonObject jsonMatch = new JsonObject();
        jsonMatch.addProperty("_id", match.getMatchID().toString());

        jsonMatch.add("matchStatus", Presentation.serializerOf(MatchStatus.class).getJsonElement(match.getMatchStatus()));

        JsonArray matchAttempts = new JsonArray();
        for(Attempt attempt : match.getMadeAttempts()) {
            matchAttempts.add(Presentation.serializerOf(Attempt.class).getJsonElement(attempt));
        }
        jsonMatch.add("attempts", matchAttempts);

        JsonArray jsonSecretCode = (JsonArray) Presentation.serializerOf(SecretCode.class).getJsonElement(match.getSecretCode());
        jsonMatch.add("secretCode", jsonSecretCode);
        return jsonMatch;
    }
}

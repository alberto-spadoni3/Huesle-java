package it.unibo.sd.project.mastermind.presentation.serializers;

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
        jsonMatch.addProperty("UUID", match.getMatchID().toString());

        jsonMatch.add("matchStatus", Presentation.serializerOf(MatchStatus.class).getJsonElement(match.getMatchStatus()));
        JsonObject matchAttempts = new JsonObject();
        for(Attempt a : match.getMadeAttempts()){
            matchAttempts.add(String.valueOf(match.getMadeAttempts().indexOf(a)), Presentation.serializerOf(Attempt.class).getJsonElement(a));
        }
        jsonMatch.add("attempts", matchAttempts);
        JsonObject jsonSecretCode = (JsonObject) Presentation.serializerOf(SecretCode.class).getJsonElement(match.getSecretCode());
        jsonMatch.add("secretCode", jsonSecretCode);
        return jsonMatch;
    }
}

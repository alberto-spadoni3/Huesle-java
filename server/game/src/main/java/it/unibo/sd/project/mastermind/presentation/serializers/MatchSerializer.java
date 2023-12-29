package it.unibo.sd.project.mastermind.presentation.serializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unibo.sd.project.mastermind.model.Attempt;
import it.unibo.sd.project.mastermind.model.match.Match;

public class MatchSerializer extends AbstractJsonSerializer<Match>{
    @Override
    protected JsonElement toJsonElement(Match match) {
        JsonObject jsonMatch = new JsonObject();
        jsonMatch.addProperty("UUID", match.getMatchID().toString());

        MatchStatusSerializer mss = new MatchStatusSerializer();
        jsonMatch.add("matchStatus", mss.toJsonElement(match.getMatchStatus()));
        JsonObject matchAttempts = new JsonObject();
        AttemptSerializer as = new AttemptSerializer();
        for(Attempt a : match.getMadeAttempts()){
            matchAttempts.add(String.valueOf(match.getMadeAttempts().indexOf(a)), as.toJsonElement(a));
        }
        jsonMatch.add("attempts", matchAttempts);
        SecretCodeSerializer secretCodeSerializer = new SecretCodeSerializer();
        JsonObject jsonSecretCode = (JsonObject) secretCodeSerializer.toJsonElement(match.getSecretCode());
        jsonMatch.add("secretCode", jsonSecretCode);
        return jsonMatch;
    }
}

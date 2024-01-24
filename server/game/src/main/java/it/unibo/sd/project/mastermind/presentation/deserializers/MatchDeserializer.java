package it.unibo.sd.project.mastermind.presentation.deserializers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unibo.sd.project.mastermind.model.match.Attempt;
import it.unibo.sd.project.mastermind.model.match.SecretCode;
import it.unibo.sd.project.mastermind.model.match.Match;
import it.unibo.sd.project.mastermind.model.match.MatchStatus;
import it.unibo.sd.project.mastermind.presentation.Presentation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MatchDeserializer extends AbstractJsonDeserializer<Match> {
    @Override
    protected Match deserializeJson(JsonElement jsonElement) {
        if (jsonElement.isJsonObject()) {
            JsonObject result = (JsonObject) jsonElement;

            UUID matchUUID = UUID.fromString(result.get("_id").getAsString());
            MatchStatus matchStatus;
            try {
                matchStatus = Presentation.deserializeAs(result.get("matchStatus").toString(), MatchStatus.class);
            } catch (Exception e){
                throw new RuntimeException("Cannot deserialize " + result.get("matchStatus") + " as MatchStatus " + e.getMessage());
            }

            List<Attempt> attempts = new ArrayList<>();
            JsonArray jsonAttempts = result.getAsJsonArray(("attempts"));
            for(JsonElement elem : jsonAttempts) {
                try {
                    attempts.add(Presentation.deserializeAs(elem.toString(), Attempt.class));
                } catch (Exception e){
                    throw new RuntimeException("Cannot deserialize " + elem + " as Attempt " + e.getMessage());
                }
            }

            SecretCode secretCode;
            try {
                secretCode = Presentation.deserializeAs(result.get("secretCode").toString(), SecretCode.class);
            } catch (Exception e) {
                throw new RuntimeException("Cannot deserialize " + result.get("secretCode") + " as SecretCode " + e.getMessage());
            }
            return new Match(matchUUID, matchStatus, attempts, secretCode);
        } else {
            throw new RuntimeException("Cannot deserialize " + jsonElement + " as Match");
        }
    }
}
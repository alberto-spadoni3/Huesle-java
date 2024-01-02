package it.unibo.sd.project.mastermind.presentation.deserializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unibo.sd.project.mastermind.model.Attempt;
import it.unibo.sd.project.mastermind.model.SecretCode;
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

            UUID matchUUID = UUID.fromString(result.get("UUID").getAsString());
            MatchStatus matchStatus = null;
            try {
                matchStatus = Presentation.deserializeAs(result.get("matchStatus").toString(), MatchStatus.class);
            } catch (Exception e){
                throw new RuntimeException("Cannot deserialize " + result.get("matchStatus") + " as MatchStatus " + e.getMessage());
            }

            List<Attempt> attempts = new ArrayList<>();
            JsonObject jsonAttempts = result.getAsJsonObject(("attempts"));
            for(int i = 0; i < jsonAttempts.size(); i++){
                Attempt attempt = null;
                try {
                    attempt = Presentation.deserializeAs(jsonAttempts.get(String.valueOf(i)).toString(), Attempt.class);
                    attempts.add(attempt);
                } catch (Exception e){
                    throw new RuntimeException("Cannot deserialize " + jsonAttempts.get(String.valueOf(i)) + " as Attempt " + e.getMessage());
                }
            }
            SecretCode sc = null;
            try {
                sc = Presentation.deserializeAs(result.get("secretCode").toString(), SecretCode.class);
            } catch (Exception e) {
                throw new RuntimeException("Cannot deserialize " + result.get("secretCode") + " as SecretCode " + e.getMessage());
            }
            return new Match(matchUUID,matchStatus,attempts,sc);
        } else {
            throw new RuntimeException("Cannot deserialize " + jsonElement + " as Match");
        }
    }
}
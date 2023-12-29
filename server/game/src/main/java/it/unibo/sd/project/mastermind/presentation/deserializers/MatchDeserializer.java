package it.unibo.sd.project.mastermind.presentation.deserializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unibo.sd.project.mastermind.model.Attempt;
import it.unibo.sd.project.mastermind.model.Player;
import it.unibo.sd.project.mastermind.model.SecretCode;
import it.unibo.sd.project.mastermind.model.match.Match;
import it.unibo.sd.project.mastermind.model.match.MatchState;
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
            MatchStatusDeserializer msd = new MatchStatusDeserializer();
            MatchStatus matchStatus = msd.deserializeJson(result.get("matchStatus").getAsJsonObject());
            List<Attempt> attempts = new ArrayList<>();
            AttemptDeserializer ad = new AttemptDeserializer();
            JsonObject jsonAttempts = result.getAsJsonObject(("attempts"));
            for(int i = 0; i < jsonAttempts.size(); i++){
                attempts.add(ad.deserializeJson(jsonAttempts.get(String.valueOf(i))));
            }
            SecretCodeDeserializer scd = new SecretCodeDeserializer();
            SecretCode sc = scd.deserializeJson(result.get("secretCode"));

            return new Match(matchUUID,matchStatus,attempts,sc);
        } else {
            throw new RuntimeException("Cannot deserialize " + jsonElement + " as Match");
        }
    }
}
package it.unibo.sd.project.mastermind.presentation.deserializers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unibo.sd.project.mastermind.model.user.Player;
import it.unibo.sd.project.mastermind.model.match.MatchState;
import it.unibo.sd.project.mastermind.model.match.MatchStatus;
import it.unibo.sd.project.mastermind.presentation.Presentation;

import java.util.ArrayList;
import java.util.List;

public class MatchStatusDeserializer extends AbstractJsonDeserializer<MatchStatus> {
    @Override
    protected MatchStatus deserializeJson(JsonElement jsonElement) {
        if (jsonElement.isJsonObject()) {
            JsonObject result = (JsonObject) jsonElement;
            MatchState matchState = MatchState.valueOf(result.get("matchState").getAsString());

            List<Player> players = new ArrayList<>();
            JsonArray jsonPlayers = result.getAsJsonArray("players");
            for(JsonElement elem : jsonPlayers){
                try {
                    players.add(Presentation.deserializeAs(elem.toString(), Player.class));
                } catch (Exception e){
                    throw new RuntimeException("Cannot deserialize " + elem + " as Player " + e.getMessage());
                }
            }

            String nextPlayer = ((JsonObject) jsonElement).get("nextPlayer").getAsString();

            MatchStatus matchStatus = new MatchStatus(players, matchState, nextPlayer);
            if (result.get("abandoned").getAsBoolean())
                matchStatus.setAbandoned();

            return matchStatus;
        } else {
            throw new RuntimeException("Cannot deserialize " + jsonElement + " as MatchStatus");
        }
    }
}

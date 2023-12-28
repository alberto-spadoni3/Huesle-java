package it.unibo.sd.project.mastermind.presentation.deserializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unibo.sd.project.mastermind.model.Player;
import it.unibo.sd.project.mastermind.model.match.MatchState;
import it.unibo.sd.project.mastermind.model.match.MatchStatus;

import java.util.ArrayList;
import java.util.List;

public class MatchStatusDeserializer extends AbstractJsonDeserializer<MatchStatus> {
    @Override
    protected MatchStatus deserializeJson(JsonElement jsonElement) {
        if (jsonElement.isJsonObject()) {
            JsonObject result = (JsonObject) jsonElement;
            MatchState matchState = MatchState.valueOf(result.get("matchState").getAsString());
            List<Player> players = new ArrayList<>();
            JsonObject jsonPlayers = result.getAsJsonObject("players");
            PlayerDeserializer ps = new PlayerDeserializer();
            System.out.println(jsonPlayers.size());
            for(int i = 0; i < jsonPlayers.size(); i++){
                 players.add(ps.deserializeJson(jsonPlayers.get(String.valueOf(i))));
            }
            JsonObject jsonNextPlayer = result.getAsJsonObject("nextPlayer");
            Player nextPlayer = ps.deserializeJson(jsonNextPlayer);


            MatchStatus ms = new MatchStatus(players);
            ms.changeNextPlayer(nextPlayer);
            ms.changeState(matchState);
            return ms;
        } else {
            throw new RuntimeException("Cannot deserialize " + jsonElement + " as MatchStatus");
        }
    }
}

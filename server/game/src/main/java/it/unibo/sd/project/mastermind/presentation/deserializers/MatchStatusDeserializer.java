package it.unibo.sd.project.mastermind.presentation.deserializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unibo.sd.project.mastermind.model.Player;
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
            JsonObject jsonPlayers = result.getAsJsonObject("players");
            for(int i = 0; i < jsonPlayers.size(); i++){
                Player player;
                try {
                    player = Presentation.deserializeAs(jsonPlayers.get(String.valueOf(i)).toString(), Player.class);
                    players.add(player);
                } catch (Exception e){
                    throw new RuntimeException("Cannot deserialize " + jsonPlayers.get(String.valueOf(i)) + " as Player " + e.getMessage());
                }
            }
            JsonObject jsonNextPlayer = result.getAsJsonObject("nextPlayer");
            Player nextPlayer;
            try {
                nextPlayer = Presentation.deserializeAs(jsonNextPlayer.toString(), Player.class);
            } catch (Exception e){
                throw new RuntimeException("Cannot deserialize " + jsonNextPlayer + " as Player " + e.getMessage());
            }

            MatchStatus ms = new MatchStatus(players);
            ms.changeNextPlayer(nextPlayer);
            ms.changeState(matchState);
            return ms;
        } else {
            throw new RuntimeException("Cannot deserialize " + jsonElement + " as MatchStatus");
        }
    }
}

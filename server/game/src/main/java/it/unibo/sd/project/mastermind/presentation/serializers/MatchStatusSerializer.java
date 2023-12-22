package it.unibo.sd.project.mastermind.presentation.serializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unibo.sd.project.mastermind.model.Player;
import it.unibo.sd.project.mastermind.model.match.MatchStatus;

public class MatchStatusSerializer extends AbstractJsonSerializer<MatchStatus> {
    @Override
    protected JsonElement toJsonElement(MatchStatus matchStatus) {
        JsonObject jsonMatchStatus = new JsonObject();
        jsonMatchStatus.addProperty("matchState", matchStatus.getMatchState().toString());

        //Serialize players
        JsonObject matchPlayers = new JsonObject();
        PlayerSerializer ps = new PlayerSerializer();
        for(Player p : matchStatus.getPlayers()){
            matchPlayers.add(String.valueOf(matchStatus.getPlayers().indexOf(p)), ps.toJsonElement(p));
        }
        jsonMatchStatus.add("players", matchPlayers);

        //Serialize next player
        JsonObject nextPlayer = new JsonObject();
        jsonMatchStatus.add("nextPlayer", ps.toJsonElement(matchStatus.getNextPlayer()));

        return jsonMatchStatus;
    }
}

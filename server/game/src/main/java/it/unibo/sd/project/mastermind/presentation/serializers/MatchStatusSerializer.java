package it.unibo.sd.project.mastermind.presentation.serializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unibo.sd.project.mastermind.model.Player;
import it.unibo.sd.project.mastermind.model.match.MatchStatus;
import it.unibo.sd.project.mastermind.presentation.Presentation;

public class MatchStatusSerializer extends AbstractJsonSerializer<MatchStatus> {
    @Override
    protected JsonElement toJsonElement(MatchStatus matchStatus) {
        JsonObject jsonMatchStatus = new JsonObject();
        jsonMatchStatus.addProperty("matchState", matchStatus.getMatchState().toString());

        //Serialize players
        JsonObject matchPlayers = new JsonObject();
        for(Player p : matchStatus.getPlayers()){
            matchPlayers.add(String.valueOf(matchStatus.getPlayers().indexOf(p)), Presentation.serializerOf(Player.class).getJsonElement(p));
        }
        jsonMatchStatus.add("players", matchPlayers);

        //Serialize next player
        jsonMatchStatus.add("nextPlayer", Presentation.serializerOf(Player.class).getJsonElement(matchStatus.getNextPlayer()));

        return jsonMatchStatus;
    }
}

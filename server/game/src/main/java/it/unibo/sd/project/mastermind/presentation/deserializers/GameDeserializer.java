package it.unibo.sd.project.mastermind.presentation.deserializers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unibo.sd.project.mastermind.model.Game;
import it.unibo.sd.project.mastermind.model.user.Player;
import it.unibo.sd.project.mastermind.model.match.Match;
import it.unibo.sd.project.mastermind.presentation.Presentation;

import java.util.ArrayList;
import java.util.List;

public class GameDeserializer extends AbstractJsonDeserializer<Game>{
    @Override
    protected Game deserializeJson(JsonElement jsonElement) {
        if(jsonElement.isJsonObject()){
            JsonObject result = (JsonObject) jsonElement;
            Player player;
            try {
                player = Presentation.deserializeAs(result.get("player").toString(), Player.class);

                JsonArray jsonActiveMatches = result.getAsJsonArray("activeMatches");
                List<Match> activeMatches = extractMatches(jsonActiveMatches);

                JsonArray jsonEndedMatches = result.getAsJsonArray("endedMatches");
                List<Match> endedMatches = extractMatches(jsonEndedMatches);

                Game game = new Game(player);
                game.setActiveMatches(activeMatches);
                game.setEndedMatches(endedMatches);
                return game;
            } catch (Exception e) {
                throw new RuntimeException("Cannot deserialize " + result.get("player") + " as Player " + e.getMessage());
            }
        } else {
            throw new RuntimeException("Cannot deserialize " + jsonElement + " as Game");
        }
    }

    private List<Match> extractMatches(JsonArray jsonMatches) {
        List<Match> matches = new ArrayList<>();
        for(JsonElement elem : jsonMatches){
            try {
                matches.add(Presentation.deserializerOf(Match.class).deserialize(elem.toString()));
            } catch (Exception e) {
                throw new RuntimeException("Cannot deserialize " + elem + " as Match - " + e.getMessage());
            }
        }
        return matches;
    }
}

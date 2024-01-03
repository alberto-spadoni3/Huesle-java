package it.unibo.sd.project.mastermind.presentation.deserializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unibo.sd.project.mastermind.model.Game;
import it.unibo.sd.project.mastermind.model.Player;
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

                JsonObject jsonActiveMatches = result.getAsJsonObject("activeMatches");
                List<Match> activeMatches = extractMatches(jsonActiveMatches);

                JsonObject jsonEndedMatches = result.getAsJsonObject("endedMatches");
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

    private List<Match> extractMatches(JsonObject jsonMatches) {
        List<Match> matches = new ArrayList<>();
        for(int i = 0; i< jsonMatches.size(); i++){
            try {
                Match match = Presentation.deserializerOf(Match.class).deserialize(jsonMatches.get(String.valueOf(i)).getAsString());
                matches.add(match);
            } catch (Exception e) {
                throw new RuntimeException("Cannot deserialize " + jsonMatches.get(String.valueOf(i)) + " as Match " + e.getMessage());
            }
        }
        return matches;
    }
}

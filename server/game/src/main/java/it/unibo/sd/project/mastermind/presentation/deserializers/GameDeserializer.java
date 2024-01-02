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
            } catch (Exception e) {
                throw new RuntimeException("Cannot deserialize " + result.get("player") + " as Player " + e.getMessage());
            }

            JsonObject jsonActiveMatches = result.getAsJsonObject("activeMatches");
            List<Match> activeMatches = new ArrayList<>();
            for(int i = 0; i< jsonActiveMatches.size(); i++){
                try {
                    Match match = Presentation.deserializerOf(Match.class).deserialize(jsonActiveMatches.get(String.valueOf(i)).getAsString());
                    activeMatches.add(match);
                } catch (Exception e) {
                    throw new RuntimeException("Cannot deserialize " + jsonActiveMatches.get(String.valueOf(i)) + " as Match " + e.getMessage());
                }
            }

            JsonObject jsonEndedMatches = result.getAsJsonObject("endedMatches");
            List<Match> endedMatches = new ArrayList<>();
            for(int i = 0; i< jsonEndedMatches.size(); i++){
                try {
                    Match match = Presentation.deserializerOf(Match.class).deserialize(jsonEndedMatches.get(String.valueOf(i)).getAsString());
                    endedMatches.add(match);
                } catch (Exception e) {
                    throw new RuntimeException("Cannot deserialize " + jsonEndedMatches.get(String.valueOf(i)) + " as Match " + e.getMessage());
                }
            }
            Game game = new Game(player);
            game.setActiveMatches(activeMatches);
            game.setEndedMatches(endedMatches);
            return game;
        } else {
            throw new RuntimeException("Cannot deserialize " + jsonElement + " as Game");
        }
    }
}

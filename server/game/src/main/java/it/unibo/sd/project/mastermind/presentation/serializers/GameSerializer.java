package it.unibo.sd.project.mastermind.presentation.serializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unibo.sd.project.mastermind.model.Game;
import it.unibo.sd.project.mastermind.model.Player;
import it.unibo.sd.project.mastermind.model.match.Match;
import it.unibo.sd.project.mastermind.presentation.Presentation;

public class GameSerializer extends AbstractJsonSerializer<Game>{
    @Override
    protected JsonElement toJsonElement(Game game) {
        JsonObject jsonGame = new JsonObject();
        jsonGame.add("player", Presentation.serializerOf(Player.class).getJsonElement(game.getPlayer()));
        JsonObject activeMatches = new JsonObject();
        for(int i = 0; i < game.getActiveMatches().size(); i++){
            activeMatches.add(String.valueOf(i), Presentation.serializerOf(Match.class).getJsonElement(game.getActiveMatches().get(i)));
        }
        jsonGame.add("activeMatches", activeMatches);
        JsonObject endedMatches = new JsonObject();
        for(int i = 0; i < game.getEndedMatches().size(); i++){
            endedMatches.add(String.valueOf(i), Presentation.serializerOf(Match.class).getJsonElement(game.getEndedMatches().get(i)));
        }
        jsonGame.add("endedMatches", endedMatches);
        return jsonGame;
    }
}

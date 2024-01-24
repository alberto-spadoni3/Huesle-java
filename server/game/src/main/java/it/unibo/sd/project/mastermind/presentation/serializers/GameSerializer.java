package it.unibo.sd.project.mastermind.presentation.serializers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unibo.sd.project.mastermind.model.Game;
import it.unibo.sd.project.mastermind.model.user.Player;
import it.unibo.sd.project.mastermind.model.match.Match;
import it.unibo.sd.project.mastermind.presentation.Presentation;

public class GameSerializer extends AbstractJsonSerializer<Game>{
    @Override
    protected JsonElement toJsonElement(Game game) {
        JsonObject jsonGame = new JsonObject();
        jsonGame.add("player", Presentation.serializerOf(Player.class).getJsonElement(game.getPlayer()));

        JsonArray activeMatches = new JsonArray();
        for(Match match : game.getActiveMatches()){
            activeMatches.add(Presentation.serializerOf(Match.class).getJsonElement(match));
        }
        jsonGame.add("activeMatches", activeMatches);

        JsonArray endedMatches = new JsonArray();
        for(Match match : game.getEndedMatches()){
            endedMatches.add(Presentation.serializerOf(Match.class).getJsonElement(match));
        }
        jsonGame.add("endedMatches", endedMatches);

        return jsonGame;
    }
}

package it.unibo.sd.project.mastermind.presentation.serializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unibo.sd.project.mastermind.model.Game;

public class GameSerializer extends AbstractJsonSerializer<Game>{
    @Override
    protected JsonElement toJsonElement(Game game) {
        JsonObject jsonGame = new JsonObject();

        return jsonGame;
    }
}

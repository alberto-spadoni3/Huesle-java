package it.unibo.sd.project.mastermind.presentation.serializers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unibo.sd.project.mastermind.model.Attempt;
import it.unibo.sd.project.mastermind.model.Hints;
import it.unibo.sd.project.mastermind.model.Player;
import it.unibo.sd.project.mastermind.presentation.Presentation;

public class AttemptSerializer extends AbstractJsonSerializer<Attempt>{
    @Override
    protected JsonElement toJsonElement(Attempt attempt) {
        JsonObject jsonAttempt = new JsonObject();

        JsonArray jsonColorSequence = new JsonArray();
        for(String s: attempt.getColorSequence()){
            jsonColorSequence.add(s);
        }
        jsonAttempt.add("colorSequence", jsonColorSequence);
        JsonObject jsonHints = (JsonObject) Presentation.serializerOf(Hints.class).getJsonElement(attempt.getHints());

        jsonAttempt.add("hints", jsonHints);
        //Player
        JsonObject jsonPlayer = (JsonObject) Presentation.serializerOf(Player.class).getJsonElement(attempt.getPlayer());
        jsonAttempt.add("attemptMadeBy", jsonPlayer);

        return jsonAttempt;
    }
}

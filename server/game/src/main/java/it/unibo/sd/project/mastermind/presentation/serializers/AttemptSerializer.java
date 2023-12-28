package it.unibo.sd.project.mastermind.presentation.serializers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unibo.sd.project.mastermind.model.Attempt;
import it.unibo.sd.project.mastermind.model.Hints;
import it.unibo.sd.project.mastermind.model.SecretCode;
import it.unibo.sd.project.mastermind.presentation.Presentation;

public class AttemptSerializer extends AbstractJsonSerializer<Attempt>{
    @Override
    protected JsonElement toJsonElement(Attempt attempt) {
        //TODO fix
        JsonObject jsonAttempt = new JsonObject();

        JsonArray jsonColorSequence = new JsonArray();
        for(String s: attempt.getColorSequence()){
            jsonColorSequence.add(s);
        }
        jsonAttempt.add("colorSequence", jsonColorSequence);
        JsonObject jsonHints;
        jsonHints = (JsonObject) new HintsSerializer().toJsonElement(attempt.getHints());
        jsonAttempt.add("hints", jsonHints);
        //Player
        JsonObject jsonPlayer;
        PlayerSerializer playerSerializer = new PlayerSerializer();
        jsonPlayer = (JsonObject) playerSerializer.toJsonElement(attempt.getPlayer());
        jsonAttempt.add("attemptMadeBy", jsonPlayer);

        return jsonAttempt;
    }
}

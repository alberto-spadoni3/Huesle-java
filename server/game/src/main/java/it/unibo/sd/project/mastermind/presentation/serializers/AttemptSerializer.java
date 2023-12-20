package it.unibo.sd.project.mastermind.presentation.serializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unibo.sd.project.mastermind.model.Attempt;

public class AttemptSerializer extends AbstractJsonSerializer<Attempt>{
    @Override
    protected JsonElement toJsonElement(Attempt attempt) {
        JsonObject jsonAttempt = new JsonObject();

        JsonObject jsonColorSequence;
        //TO-DO add ColorSequenceSerializer

        JsonObject jsonHints;
        //TO-DO add HintsSerializer

        //Player
        JsonObject jsonPlayer;
        PlayerSerializer playerSerializer = new PlayerSerializer();
        jsonPlayer = (JsonObject) playerSerializer.toJsonElement(attempt.getPlayer());
        jsonAttempt.add("attemptMadeBy", jsonPlayer);

        return jsonAttempt;
    }
}

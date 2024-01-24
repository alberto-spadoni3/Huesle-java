package it.unibo.sd.project.mastermind.presentation.serializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unibo.sd.project.mastermind.model.match.Hints;

public class HintsSerializer extends AbstractJsonSerializer<Hints> {
    @Override
    protected JsonElement toJsonElement(Hints hints) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("rightPositions", hints.getRightPositions());
        jsonObject.addProperty("rightColours", hints.getRightColours());
        return jsonObject;
    }
}

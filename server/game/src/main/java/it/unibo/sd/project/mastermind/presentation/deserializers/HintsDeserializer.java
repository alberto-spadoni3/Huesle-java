package it.unibo.sd.project.mastermind.presentation.deserializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unibo.sd.project.mastermind.model.match.Hints;

public class HintsDeserializer extends AbstractJsonDeserializer<Hints> {
    @Override
    protected Hints deserializeJson(JsonElement jsonElement) {
        if (jsonElement.isJsonObject()) {
            JsonObject result = (JsonObject) jsonElement;
            byte rightPositions = result.get("rightPositions").getAsByte();
            byte rightColours = result.get("rightColours").getAsByte();
            return new Hints(rightPositions, rightColours);
        } else {
            throw new RuntimeException("Cannot deserialize " + jsonElement + " as Hints");
        }
    }
}
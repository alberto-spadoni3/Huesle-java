package it.unibo.sd.project.mastermind.presentation.deserializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unibo.sd.project.mastermind.model.SecretCode;

import java.util.ArrayList;

public class SecretCodeDeserializer extends AbstractJsonDeserializer<SecretCode> {
    @Override
    protected SecretCode deserializeJson(JsonElement jsonElement) {
        if (jsonElement.isJsonObject()) {
            JsonObject result = (JsonObject) jsonElement;
            ArrayList<String> sequence = new ArrayList<>();
            //TODO After modifying serializer with JSONArray

            return new SecretCode(sequence);
        } else {
            throw new RuntimeException("Cannot deserialize " + jsonElement + " as SecretCode");
        }
    }
}

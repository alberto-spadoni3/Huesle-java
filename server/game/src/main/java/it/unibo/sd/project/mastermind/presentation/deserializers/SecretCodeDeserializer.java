package it.unibo.sd.project.mastermind.presentation.deserializers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import it.unibo.sd.project.mastermind.model.match.SecretCode;

import java.util.ArrayList;

public class SecretCodeDeserializer extends AbstractJsonDeserializer<SecretCode> {
    @Override
    protected SecretCode deserializeJson(JsonElement jsonElement) {
        if (jsonElement.isJsonArray()) {
            JsonArray result = (JsonArray) jsonElement;
            ArrayList<String> sequence = new ArrayList<>();
            for (JsonElement e : result) {
                sequence.add(e.getAsString());
            }
            return new SecretCode(sequence);
        } else {
            throw new RuntimeException("Cannot deserialize " + jsonElement + " as SecretCode");
        }
    }
}

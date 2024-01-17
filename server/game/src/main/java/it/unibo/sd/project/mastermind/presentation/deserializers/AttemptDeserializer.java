package it.unibo.sd.project.mastermind.presentation.deserializers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unibo.sd.project.mastermind.model.Attempt;
import it.unibo.sd.project.mastermind.model.Hints;
import it.unibo.sd.project.mastermind.presentation.Presentation;

import java.util.ArrayList;
import java.util.List;

public class AttemptDeserializer extends AbstractJsonDeserializer<Attempt>{
    @Override
    protected Attempt deserializeJson(JsonElement jsonElement) {
        if(jsonElement.isJsonObject()){
            JsonObject result = (JsonObject) jsonElement;
            List<String> colorSequence = new ArrayList<>();
            JsonArray jsonColorSequence = result.getAsJsonArray("colorSequence");
            for (JsonElement elem : jsonColorSequence)
                colorSequence.add(elem.getAsString());

            String attemptMadeBy = result.get("madeBy").getAsString();
            Hints hints = null;
            if (result.has("hints") && result.get("hints").isJsonObject()) {
                try {
                    hints = Presentation.deserializeAs(result.get("hints").toString(), Hints.class);
                } catch (Exception e) {
                    throw new RuntimeException("Cannot deserialize " + jsonElement + " - " + e.getMessage());
                }
            }
            return new Attempt(colorSequence, attemptMadeBy, hints);
        } else {
            throw new RuntimeException("Cannot deserialize " + jsonElement + " as Attempt");
        }
    }
}

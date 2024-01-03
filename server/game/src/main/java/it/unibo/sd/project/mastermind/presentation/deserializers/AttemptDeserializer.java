package it.unibo.sd.project.mastermind.presentation.deserializers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unibo.sd.project.mastermind.model.Attempt;
import it.unibo.sd.project.mastermind.model.Hints;
import it.unibo.sd.project.mastermind.model.Player;
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
//            for(int i=0; i<jsonColorSequence.size(); i++){
//                colorSequence.add(jsonColorSequence.get(i).getAsString());
//            }
            for (JsonElement elem : jsonColorSequence)
                colorSequence.add(elem.getAsString());
            Player attemptMadeBy = null;
            Hints hints = null;
            try {
                attemptMadeBy = Presentation.deserializeAs(result.get("attemptMadeBy").toString(), Player.class);
            } catch (Exception e) {
                throw new RuntimeException("Cannot deserialize " + result.get("attemptMadeBy") + " as Player " + e.getMessage());
            }
            try {
                hints = Presentation.deserializeAs(result.get("hints").toString(), Hints.class);
            } catch (Exception e){
                throw new RuntimeException("Cannot deserialize " + result.get("hints") + " as Hints " + e.getMessage());
            }

            return new Attempt(colorSequence,attemptMadeBy,hints);
        } else {
            throw new RuntimeException("Cannot deserialize " + jsonElement + " as Attempt");
        }
    }
}

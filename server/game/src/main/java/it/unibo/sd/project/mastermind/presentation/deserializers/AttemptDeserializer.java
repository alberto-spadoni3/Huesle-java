package it.unibo.sd.project.mastermind.presentation.deserializers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unibo.sd.project.mastermind.model.Attempt;
import it.unibo.sd.project.mastermind.model.Hints;
import it.unibo.sd.project.mastermind.model.Player;
import it.unibo.sd.project.mastermind.model.SecretCode;
import it.unibo.sd.project.mastermind.presentation.Presentation;
import it.unibo.sd.project.mastermind.presentation.serializers.PlayerSerializer;

import java.util.ArrayList;
import java.util.List;

public class AttemptDeserializer extends AbstractJsonDeserializer<Attempt>{
    @Override
    protected Attempt deserializeJson(JsonElement jsonElement) {
        if(jsonElement.isJsonObject()){
            JsonObject result = (JsonObject) jsonElement;
            List<String> colorSequence = new ArrayList<>();
            JsonArray jsonColorSequence = result.getAsJsonArray("colorSequence");
            for(int i=0; i<jsonColorSequence.size(); i++){
                colorSequence.add(jsonColorSequence.get(i).getAsString());
            }
            PlayerDeserializer pd = new PlayerDeserializer();
            Player attemptMadeBy = pd.deserializeJson(result.get("attemptMadeBy"));
            HintsDeserializer hd = new HintsDeserializer();
            Hints hints = hd.deserializeJson(result.get("hints"));
            return new Attempt(colorSequence,attemptMadeBy,hints);
        } else {
            throw new RuntimeException("Cannot deserialize " + jsonElement + " as Attempt");
        }
    }
}

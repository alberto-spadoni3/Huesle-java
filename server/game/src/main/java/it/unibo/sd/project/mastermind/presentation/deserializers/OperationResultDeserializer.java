package it.unibo.sd.project.mastermind.presentation.deserializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unibo.sd.project.mastermind.model.user.OperationResult;

public class OperationResultDeserializer extends AbstractJsonDeserializer<OperationResult> {
    @Override
    protected OperationResult deserializeJson(JsonElement jsonElement) {
        if (jsonElement.isJsonObject()) {
            JsonObject result = (JsonObject) jsonElement;
            short statusCode = result.get("statusCode").getAsShort();
            String message = result.get("resultMessage").getAsString();
            return new OperationResult(statusCode, message);
        } else {
            throw new RuntimeException("Cannot deserialize " + jsonElement + " as OperationResult");
        }
    }
}

package it.unibo.sd.project.mastermind.presentation.serializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unibo.sd.project.mastermind.model.user.OperationResult;

public class OperationResultSerializer extends AbstractJsonSerializer<OperationResult> {
    @Override
    protected JsonElement toJsonElement(OperationResult object) {
        JsonObject jsonOpResult = new JsonObject();
        jsonOpResult.addProperty("statusCode", object.getStatusCode());
        jsonOpResult.addProperty("resultMessage", object.getResultMessage());
        return jsonOpResult;
    }
}

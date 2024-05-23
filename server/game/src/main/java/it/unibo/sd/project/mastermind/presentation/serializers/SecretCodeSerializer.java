package it.unibo.sd.project.mastermind.presentation.serializers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import it.unibo.sd.project.mastermind.model.match.SecretCode;

public class SecretCodeSerializer extends AbstractJsonSerializer<SecretCode> {
    @Override
    protected JsonElement toJsonElement(SecretCode secretCode) {
        JsonArray jsonColorCode = new JsonArray();
        for (String s : secretCode.getCode()) {
            jsonColorCode.add(s);
        }
        return jsonColorCode;
    }
}

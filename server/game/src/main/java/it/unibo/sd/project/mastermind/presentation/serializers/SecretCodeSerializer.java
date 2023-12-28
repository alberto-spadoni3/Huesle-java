package it.unibo.sd.project.mastermind.presentation.serializers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unibo.sd.project.mastermind.model.SecretCode;

public class SecretCodeSerializer extends AbstractJsonSerializer<SecretCode> {
    @Override
    protected JsonElement toJsonElement(SecretCode secretCode) {
        JsonObject jsonSecretCode = new JsonObject();
        JsonArray jsonColorCode = new JsonArray();
        for(String s : secretCode.getCode()){
            jsonColorCode.add(s);
        }
        jsonSecretCode.add("colorCode", jsonColorCode);
        return jsonSecretCode;
    }
}

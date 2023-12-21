package it.unibo.sd.project.mastermind.presentation.serializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unibo.sd.project.mastermind.model.SecretCode;

public class SecretCodeSerializer extends AbstractJsonSerializer<SecretCode> {
    @Override
    protected JsonElement toJsonElement(SecretCode secretCode) {
        JsonObject jsonSecretCode = new JsonObject();
        String stringSecretCode = new String();
        for(String s : secretCode.getCode()){
            stringSecretCode += s;
            if(!s.equals(secretCode.getCode().get(secretCode.getCOLOR_SEQUENCE_LENGTH() - 1))){
                stringSecretCode += ",";
            }
        }
        jsonSecretCode.addProperty("colorCode", stringSecretCode);
        return jsonSecretCode;
    }
}

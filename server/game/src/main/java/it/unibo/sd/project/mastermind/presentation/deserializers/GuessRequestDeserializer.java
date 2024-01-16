package it.unibo.sd.project.mastermind.presentation.deserializers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unibo.sd.project.mastermind.model.Attempt;
import it.unibo.sd.project.mastermind.model.GuessRequest;
import it.unibo.sd.project.mastermind.model.user.LoginRequest;
import it.unibo.sd.project.mastermind.presentation.Presentation;

public class GuessRequestDeserializer extends AbstractJsonDeserializer<GuessRequest> {
    @Override
    protected GuessRequest deserializeJson(JsonElement jsonElement) {
        if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = (JsonObject) jsonElement;
            String requesterUsername = jsonObject.get("requesterUsername").getAsString();
            if (jsonObject.has("sequence")) {
                JsonArray jsonSequence = jsonObject.getAsJsonArray("sequence");
                Attempt madeAttempt = null;
                try {
                    madeAttempt = Presentation.deserializeAs(jsonSequence.toString(), Attempt.class);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
                return new GuessRequest(requesterUsername, madeAttempt);
            } else
                throw new RuntimeException("Cannot deserialize as " + LoginRequest.class.getName());
        } else
            throw new RuntimeException("Cannot deserialize as " + LoginRequest.class.getName());
    }
}

package it.unibo.sd.project.mastermind.presentation.deserializers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unibo.sd.project.mastermind.model.match.Attempt;
import it.unibo.sd.project.mastermind.model.request.GuessRequest;
import it.unibo.sd.project.mastermind.presentation.Presentation;

public class GuessRequestDeserializer extends AbstractJsonDeserializer<GuessRequest> {
    @Override
    protected GuessRequest deserializeJson(JsonElement jsonElement) {
        if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = (JsonObject) jsonElement;
            String requesterUsername = jsonObject.get("requesterUsername").getAsString();
            if (jsonObject.has("matchID") && jsonObject.has("colorSequence")) {
                String matchID = jsonObject.get("matchID").getAsString();
                JsonObject jsonAttempt = new JsonObject();
                JsonArray jsonSequence = jsonObject.getAsJsonArray("colorSequence");
                jsonAttempt.addProperty("madeBy", requesterUsername);
                jsonAttempt.add("colorSequence", jsonSequence);
                Attempt madeAttempt = null;
                try {
                    madeAttempt = Presentation.deserializeAs(jsonAttempt.toString(), Attempt.class);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
                return new GuessRequest(requesterUsername, matchID, madeAttempt);
            } else
                throw new RuntimeException("Cannot deserialize as " + GuessRequest.class.getName());
        } else
            throw new RuntimeException("Cannot deserialize as " + GuessRequest.class.getName());
    }
}

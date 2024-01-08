package it.unibo.sd.project.mastermind.presentation.deserializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unibo.sd.project.mastermind.model.match.SearchRequest;

public class SearchRequestDeserializer extends AbstractJsonDeserializer<SearchRequest> {
    @Override
    protected SearchRequest deserializeJson(JsonElement jsonElement) {
        if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = (JsonObject) jsonElement;
            SearchRequest searchRequest;
            String requesterUsername = jsonObject.get("requesterUsername").getAsString();
            boolean isMatchPrivate = false;
            if (jsonObject.has("isPrivateMatch"))
                isMatchPrivate = jsonObject.get("isPrivateMatch").getAsBoolean();
            if (jsonObject.has("matchAccessCode"))
                searchRequest = new SearchRequest(
                        requesterUsername,
                        jsonObject.get("matchAccessCode").getAsString());
            else
                searchRequest = new SearchRequest(requesterUsername, isMatchPrivate);
            return searchRequest;
        } else {
            throw new RuntimeException("Cannot deserialize " + jsonElement + " as SearchRequest");
        }
    }
}

package it.unibo.sd.project.mastermind.presentation.deserializers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class AbstractJsonDeserializer<T> implements Deserializer<T> {
    private final Gson gson = new Gson();

    private JsonElement toJsonElement(String string) {
        return gson.fromJson(string, JsonElement.class);
    }

    @Override
    public T deserialize(String string) {
        var jsonElement = toJsonElement(string);
        return deserializeJson(jsonElement);
    }

    protected abstract T deserializeJson(JsonElement jsonElement);

    @Override
    public List<T> deserializeMany(String string) {
        JsonArray jsonArray = (JsonArray) toJsonElement(string);
        return IntStream.range(0, jsonArray.size())
                .mapToObj(jsonArray::get)
                .map(this::deserializeJson)
                .collect(Collectors.toList());
    }
}

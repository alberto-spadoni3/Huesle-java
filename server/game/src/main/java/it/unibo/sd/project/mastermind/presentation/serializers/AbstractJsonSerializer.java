package it.unibo.sd.project.mastermind.presentation.serializers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.Collection;

public abstract class AbstractJsonSerializer<T> implements Serializer<T> {
    private final Gson gson = new Gson();

    protected abstract JsonElement toJsonElement(T object);

    @Override
    public String serialize(T object) {
        return gson.toJson(toJsonElement(object));
    }

    @Override
    public JsonElement getJsonElement(T object) {
        return toJsonElement(object);
    }

    @Override
    public String serializeMany(Collection<? extends T> objects) {
        var jsonArray = new JsonArray();
        for (var object : objects) {
            jsonArray.add(toJsonElement(object));
        }
        return gson.toJson(jsonArray);
    }
}

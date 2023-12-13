package it.unibo.sd.project.mastermind.presentation;

import it.unibo.sd.project.mastermind.model.Player;
import it.unibo.sd.project.mastermind.model.user.LoginRequest;
import it.unibo.sd.project.mastermind.presentation.deserializers.Deserializer;
import it.unibo.sd.project.mastermind.presentation.deserializers.LoginRequestDeserializer;
import it.unibo.sd.project.mastermind.presentation.deserializers.PlayerDeserializer;
import it.unibo.sd.project.mastermind.presentation.serializers.PlayerSerializer;
import it.unibo.sd.project.mastermind.presentation.serializers.Serializer;
import java.util.HashMap;
import java.util.Map;

public class Presentation {
    private static final Map<Class<?>, Serializer<?>> serializers = new HashMap<>();
    private static final Map<Class<?>, Deserializer<?>> deserializers = new HashMap<>();

    static {
        registerSerializers();
        registerDeserializers();
    }

    private static void registerSerializers() {
        //TODO
        serializers.put(Player.class, new PlayerSerializer());
    }

    private static void registerDeserializers() {
        //TODO
        deserializers.put(Player.class, new PlayerDeserializer());
        deserializers.put(LoginRequest.class, new LoginRequestDeserializer());
    }

    public static <T> Serializer<T> serializerOf(Class<T> klass) {
        if (!serializers.containsKey(klass)) {
            serializers.keySet().stream()
                    .filter(key -> key.isAssignableFrom(klass))
                    .map(serializers::get)
                    .findAny()
                    .map(klass::cast)
                    .orElseThrow(() -> new IllegalArgumentException("No available serializer for class: " + klass.getName()));
        }
        return (Serializer<T>) serializers.get(klass);
    }

    public static <T> Deserializer<T> deserializerOf(Class<T> klass) {
        if (!deserializers.containsKey(klass)) {
            deserializers.keySet().stream()
                    .filter(key -> key.isAssignableFrom(klass))
                    .map(deserializers::get)
                    .findAny()
                    .map(klass::cast)
                    .orElseThrow(() -> new IllegalArgumentException("No available deserializer for class: " + klass.getName()));
        }
        return (Deserializer<T>) deserializers.get(klass);
    }

    public static <T> T deserializeAs(String string, Class<T> type) throws Exception {
        try {
            return deserializerOf(type).deserialize(string);
        } catch (PresentationException e) {
            throw new Exception("Cannot deserialize " + string + " as " + type.getSimpleName());
        }
    }
}

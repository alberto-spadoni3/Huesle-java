package it.unibo.sd.project.mastermind.presentation;

import it.unibo.sd.project.mastermind.model.*;
import it.unibo.sd.project.mastermind.model.match.Match;
import it.unibo.sd.project.mastermind.model.match.MatchStatus;
import it.unibo.sd.project.mastermind.model.user.LoginRequest;
import it.unibo.sd.project.mastermind.model.user.OperationResult;
import it.unibo.sd.project.mastermind.presentation.deserializers.*;
import it.unibo.sd.project.mastermind.presentation.serializers.*;

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
        serializers.put(AccessibilitySettings.class, new AccessibilitySettingsSerializer());
        serializers.put(Attempt.class, new AttemptSerializer());
        serializers.put(Hints.class, new HintsSerializer());
        serializers.put(Game.class, new GameSerializer());
        serializers.put(Player.class, new PlayerSerializer());
        serializers.put(SecretCode.class, new SecretCodeSerializer());

        serializers.put(Match.class, new MatchSerializer());
        serializers.put(MatchStatus.class, new MatchStatusSerializer());

        serializers.put(OperationResult.class, new OperationResultSerializer());




    }

    private static void registerDeserializers() {
        //TODO
        deserializers.put(Player.class, new PlayerDeserializer());
        deserializers.put(LoginRequest.class, new LoginRequestDeserializer());
        deserializers.put(OperationResult.class, new OperationResultDeserializer());

        deserializers.put(AccessibilitySettings.class, new AccessibilitySettingsDeserializer());
        deserializers.put(Hints.class, new HintsDeserializer());
        deserializers.put(MatchStatus.class, new MatchStatusDeserializer());
        deserializers.put(Match.class, new MatchDeserializer());
        deserializers.put(SecretCode.class, new SecretCodeDeserializer());
        deserializers.put(Attempt.class, new AttemptDeserializer());
        deserializers.put(Game.class, new GameDeserializer());

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

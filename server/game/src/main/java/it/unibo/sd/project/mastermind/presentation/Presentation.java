package it.unibo.sd.project.mastermind.presentation;

import it.unibo.sd.project.mastermind.model.user.SettingsRequest;
import it.unibo.sd.project.mastermind.model.*;
import it.unibo.sd.project.mastermind.model.match.*;
import it.unibo.sd.project.mastermind.model.user.AccessibilitySettings;
import it.unibo.sd.project.mastermind.model.user.LoginRequest;
import it.unibo.sd.project.mastermind.model.user.Player;
import it.unibo.sd.project.mastermind.model.user.UserOperationResult;
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
        serializers.put(Player.class, new PlayerSerializer());

        serializers.put(Game.class, new GameSerializer());
        serializers.put(Match.class, new MatchSerializer());
        serializers.put(MatchStatus.class, new MatchStatusSerializer());
        serializers.put(SecretCode.class, new SecretCodeSerializer());
        serializers.put(AccessibilitySettings.class, new AccessibilitySettingsSerializer());
        serializers.put(Attempt.class, new AttemptSerializer());
        serializers.put(Hints.class, new HintsSerializer());

        serializers.put(UserOperationResult.class, new OperationResultSerializer());
        serializers.put(MatchOperationResult.class, new OperationResultSerializer());
        serializers.put(GuessOperationResult.class, new OperationResultSerializer());

        serializers.put(PendingMatchRequest.class, new PendingMatchRequestSerializer());
    }

    private static void registerDeserializers() {
        deserializers.put(Player.class, new PlayerDeserializer());

        deserializers.put(UserOperationResult.class, new OperationResultDeserializer());
        deserializers.put(MatchOperationResult.class, new OperationResultDeserializer());
        deserializers.put(GuessOperationResult.class, new OperationResultDeserializer());

        deserializers.put(Game.class, new GameDeserializer());
        deserializers.put(Match.class, new MatchDeserializer());
        deserializers.put(MatchStatus.class, new MatchStatusDeserializer());
        deserializers.put(SecretCode.class, new SecretCodeDeserializer());
        deserializers.put(AccessibilitySettings.class, new AccessibilitySettingsDeserializer());
        deserializers.put(Attempt.class, new AttemptDeserializer());
        deserializers.put(Hints.class, new HintsDeserializer());

        deserializers.put(LoginRequest.class, new LoginRequestDeserializer());
        deserializers.put(MatchRequest.class, new MatchRequestDeserializer());
        deserializers.put(PendingMatchRequest.class, new PendingMatchRequestDeserializer());
        deserializers.put(GuessRequest.class, new GuessRequestDeserializer());
        deserializers.put(SettingsRequest.class, new SettingsRequestDeserializer());
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

package it.unibo.sd.project.mastermind.model;

import it.unibo.sd.project.mastermind.rabbit.MessageType;

import java.util.Map;
import java.util.function.Function;

public interface Manager<T> {
    void init(String collectionName, Class<T> objectType);
    private Map<MessageType, Function<String, String>> getManagementCallbacks() {
        throw new RuntimeException("Not implemented");
    }
}

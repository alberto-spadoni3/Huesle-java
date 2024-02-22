package it.unibo.sd.project.mastermind;

import it.unibo.sd.project.mastermind.rabbit.MessageType;

import java.util.Map;
import java.util.function.Function;

public interface Manager {
    void init();
    void initForTesting();

    Map<MessageType, Function<String, String>> getManagementCallbacks();
}

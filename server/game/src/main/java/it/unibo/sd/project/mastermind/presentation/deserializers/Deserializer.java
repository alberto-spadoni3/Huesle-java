package it.unibo.sd.project.mastermind.presentation.deserializers;

import java.util.List;

public interface Deserializer<T> {
    T deserialize(String string);

    List<T> deserializeMany(String string);
}

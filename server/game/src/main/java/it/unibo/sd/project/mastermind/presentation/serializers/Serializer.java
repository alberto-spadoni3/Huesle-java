package it.unibo.sd.project.mastermind.presentation.serializers;

import java.util.Collection;
import java.util.List;

public interface Serializer<T> {
    String serialize(T object);

    default String serializeMany(T... objects) {
        return serializeMany(List.of(objects));
    }

    String serializeMany(Collection<? extends T> objects);
}

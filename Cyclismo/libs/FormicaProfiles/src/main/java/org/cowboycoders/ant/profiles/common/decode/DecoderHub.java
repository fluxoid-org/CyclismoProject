package org.cowboycoders.ant.profiles.common.decode;

import java.util.HashMap;

public abstract class DecoderHub<T> {

    private HashMap<Class<?>, Decoder<T>> mappings = new HashMap<>();

    public <E extends T> Decoder<T> getDecoder(E instance) {
        Class<?> clazz = instance.getClass();
        if (!mappings.containsKey(instance.getClass())) {
            mappings.put(instance.getClass(), makeDecoder());
        }
        return mappings.get(clazz);
    }

    public abstract Decoder<T> makeDecoder();
}

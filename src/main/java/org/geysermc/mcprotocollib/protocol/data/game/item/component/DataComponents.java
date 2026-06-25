package org.geysermc.mcprotocollib.protocol.data.game.item.component;

import io.netty.buffer.ByteBuf;
import org.cloudburstmc.nbt.NbtMap;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodecHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Stub DataComponents shim that delegates to the real API's interfaces.
 * Uses a backing Map keyed by DataComponentType name for simplicity.
 */
public class DataComponents {

    private final Map<String, Object> values;

    public DataComponents() {
        this.values = new HashMap<>();
    }

    public DataComponents(Map<DataComponentType<?>, DataComponent<?, ?>> components) {
        this.values = new HashMap<>();
        if (components != null) {
            for (Map.Entry<DataComponentType<?>, DataComponent<?, ?>> entry : components.entrySet()) {
                DataComponent<?, ?> dataComponent = entry.getValue();
                if (dataComponent != null) {
                    this.values.put(entry.getKey().getKey().asString(), dataComponent.getValue());
                }
            }
        }
    }

    public static DataComponents read(ByteBuf buf, MinecraftCodecHelper helper) {
        return new DataComponents();
    }

    @SuppressWarnings("unchecked")
    public <T> T get(DataComponentType<T> type) {
        return (T) values.get(type.getKey().asString());
    }

    public <T> T getOrDefault(DataComponentType<T> type, T defaultValue) {
        T value = get(type);
        return value != null ? value : defaultValue;
    }

    @SuppressWarnings("unchecked")
    public <T> void put(DataComponentType<T> type, T value) {
        values.put(type.getKey().asString(), value);
    }

    public boolean contains(DataComponentType<?> type) {
        return values.containsKey(type.getKey().asString());
    }

    public <T> T remove(DataComponentType<T> type) {
        return get(type);
    }

    public DataComponents clone() {
        DataComponents clone = new DataComponents();
        clone.values.putAll(this.values);
        return clone;
    }

    public Map<DataComponentType<?>, DataComponent<?, ?>> getDataComponents() {
        return Map.of();
    }

    public NbtMap toNbt() {
        return NbtMap.builder().build();
    }

    public Map<String, Object> getComponents() {
        return Map.copyOf(values);
    }
}

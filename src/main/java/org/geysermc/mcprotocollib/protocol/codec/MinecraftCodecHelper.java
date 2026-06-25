package org.geysermc.mcprotocollib.protocol.codec;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.kyori.adventure.text.Component;
import org.geysermc.mcprotocollib.auth.GameProfile;

import java.io.IOException;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.ChunkSection;
import org.geysermc.mcprotocollib.protocol.data.game.item.ItemStack;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.ToIntFunction;

public class MinecraftCodecHelper {

    public final Int2ObjectMap<?> levelEvents;
    public final Map<String, ?> soundNames;
    public Object registry;

    public MinecraftCodecHelper(Int2ObjectMap<?> levelEvents, Map<String, ?> soundNames) {
        this.levelEvents = levelEvents;
        this.soundNames = soundNames;
    }

    public interface ObjIntConsumer<T> {
        void accept(T t, int value);
    }

    public int readVarInt(ByteBuf buf) { return MinecraftTypes.readVarInt(buf); }
    public void writeVarInt(ByteBuf buf, int value) { MinecraftTypes.writeVarInt(buf, value); }
    public UUID readUUID(ByteBuf buf) { return MinecraftTypes.readUUID(buf); }
    public void writeUUID(ByteBuf buf, UUID uuid) { MinecraftTypes.writeUUID(buf, uuid); }
    public String readString(ByteBuf buf) { return MinecraftTypes.readString(buf); }
    public String readString(ByteBuf buf, int maxLength) { return MinecraftTypes.readString(buf, maxLength); }
    public void writeString(ByteBuf buf, String value) { MinecraftTypes.writeString(buf, value); }
    public byte[] readByteArray(ByteBuf buf) { return MinecraftTypes.readByteArray(buf); }
    public byte[] readByteArray(ByteBuf buf, ToIntFunction<ByteBuf> maxReader) { return MinecraftTypes.readByteArray(buf, maxReader); }
    public void writeByteArray(ByteBuf buf, byte[] value) { MinecraftTypes.writeByteArray(buf, value); }
    public <T> T readNullable(ByteBuf buf, Function<ByteBuf, T> reader) { return MinecraftTypes.readNullable(buf, reader); }
    public <T> void writeNullable(ByteBuf buf, T value, BiConsumer<ByteBuf, T> writer) { MinecraftTypes.writeNullable(buf, value, writer); }
    public Component readComponent(ByteBuf buf) throws IOException { return MinecraftTypes.readComponent(buf); }
    public void writeComponent(ByteBuf buf, Component value) throws IOException { MinecraftTypes.writeComponent(buf, value); }
    public <E extends Enum<E>> EnumSet<E> readEnumSet(ByteBuf buf, Class<E> enumClass) { return MinecraftTypes.readEnumSet(buf, enumClass); }
    public <E extends Enum<E>> void writeEnumSet(ByteBuf buf, EnumSet<E> enumSet, E[] values) { MinecraftTypes.writeEnumSet(buf, enumSet, values); }
    public GameProfile.Property readProperty(ByteBuf buf) { return MinecraftTypes.readProperty(buf); }
    public void writeProperty(ByteBuf buf, GameProfile.Property property) { MinecraftTypes.writeProperty(buf, property); }
    public ItemStack readItemStack(ByteBuf buf) { return MinecraftTypes.readItemStack(buf); }
    public void writeItemStack(ByteBuf buf, ItemStack itemStack) { MinecraftTypes.writeItemStack(buf, itemStack); }
    public ChunkSection readChunkSection(ByteBuf buf, int bits) { return MinecraftTypes.readChunkSection(buf, bits, bits); }
}

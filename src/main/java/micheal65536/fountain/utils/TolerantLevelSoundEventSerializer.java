package micheal65536.fountain.utils;

import io.netty.buffer.ByteBuf;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodec;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.BedrockPacketSerializer;
import org.cloudburstmc.protocol.bedrock.data.SoundEvent;
import org.cloudburstmc.protocol.bedrock.packet.LevelSoundEventPacket;
import org.cloudburstmc.protocol.common.util.TypeMap;
import org.cloudburstmc.protocol.common.util.VarInts;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

public final class TolerantLevelSoundEventSerializer implements BedrockPacketSerializer<LevelSoundEventPacket>
{
	private final TypeMap<SoundEvent> typeMap;

	private TolerantLevelSoundEventSerializer(@NotNull TypeMap<SoundEvent> typeMap)
	{
		this.typeMap = typeMap;
	}

	@SuppressWarnings("unchecked")
	@NotNull
	public static TolerantLevelSoundEventSerializer from(@NotNull BedrockCodec codec)
	{
		BedrockPacketSerializer<LevelSoundEventPacket> original = codec.getPacketDefinition(LevelSoundEventPacket.class).getSerializer();
		for (Class<?> type = original.getClass(); type != null; type = type.getSuperclass())
		{
			for (Field field : type.getDeclaredFields())
			{
				if (TypeMap.class.isAssignableFrom(field.getType()))
				{
					try
					{
						field.setAccessible(true);
						return new TolerantLevelSoundEventSerializer((TypeMap<SoundEvent>) field.get(original));
					}
					catch (IllegalAccessException exception)
					{
						throw new IllegalStateException(exception);
					}
				}
			}
		}
		throw new IllegalStateException("No sound event map in " + original.getClass().getName());
	}

	@Override
	public void serialize(ByteBuf buffer, BedrockCodecHelper helper, LevelSoundEventPacket packet)
	{
		VarInts.writeUnsignedInt(buffer, this.typeMap.getId(packet.getSound()));
		helper.writeVector3f(buffer, packet.getPosition());
		VarInts.writeInt(buffer, packet.getExtraData());
		helper.writeString(buffer, packet.getIdentifier());
		buffer.writeBoolean(packet.isBabySound());
		buffer.writeBoolean(packet.isRelativeVolumeDisabled());
	}

	@Override
	public void deserialize(ByteBuf buffer, BedrockCodecHelper helper, LevelSoundEventPacket packet)
	{
		SoundEvent sound = this.typeMap.getTypeUnsafe(VarInts.readUnsignedInt(buffer));
		packet.setSound(sound != null ? sound : SoundEvent.UNDEFINED);
		packet.setPosition(helper.readVector3f(buffer));
		packet.setExtraData(VarInts.readInt(buffer));
		packet.setIdentifier(helper.readString(buffer));
		packet.setBabySound(buffer.readBoolean());
		packet.setRelativeVolumeDisabled(buffer.readBoolean());
	}
}

package micheal65536.fountain;

import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftTypes;
import org.geysermc.mcprotocollib.protocol.data.game.entity.Effect;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundUpdateMobEffectPacket;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * Custom packet that intercepts ClientboundUpdateMobEffectPacket to handle
 * effect IDs beyond the vanilla range (custom modded effects).
 */
public class ClientboundUpdateMobEffectCustomPacket implements MinecraftPacket
{
	public static ClientboundUpdateMobEffectCustomPacket read(ByteBuf in) 
	{
		int mark = in.readerIndex();
		int entityId = MinecraftTypes.readVarInt(in);
		int effectId = MinecraftTypes.readVarInt(in);

		if (effectId >= Effect.values().length)
		{
			// Skip the rest of the packet data
			int amplifier = MinecraftTypes.readVarInt(in);
			int duration = MinecraftTypes.readVarInt(in);
			byte flags = in.readByte();
			boolean ambient = (flags & 0x01) != 0;
			boolean showParticles = (flags & 0x02) != 0;
			boolean showIcon = (flags & 0x04) != 0;
			boolean blend = (flags & 0x08) != 0;
			// Skip hidden effect (optional)
			if (in.readBoolean())
			{
				MinecraftTypes.readVarInt(in); // skip hidden effect id
				MinecraftTypes.readVarInt(in); // skip hidden effect amplifier
				MinecraftTypes.readVarInt(in); // skip hidden effect duration
				in.readByte(); // skip hidden effect flags
			}
			return new ClientboundUpdateMobEffectCustomPacket(entityId, effectId, amplifier, duration, ambient, showParticles, showIcon, blend);
		}
		else
		{
			in.readerIndex(mark);
			return null;
		}
	}

	public final int entityId;
	public final int effectId;
	public final int amplifier;
	public final int duration;
	public final boolean ambient;
	public final boolean showParticles;
	public final boolean showIcon;
	public final boolean blend;
	private final boolean hasVanillaEffect;

	private ClientboundUpdateMobEffectCustomPacket(int entityId, int effectId, int amplifier, int duration, boolean ambient, boolean showParticles, boolean showIcon, boolean blend)
	{
		this.entityId = entityId;
		this.effectId = effectId;
		this.amplifier = amplifier;
		this.duration = duration;
		this.ambient = ambient;
		this.showParticles = showParticles;
		this.showIcon = showIcon;
		this.blend = blend;
		this.hasVanillaEffect = effectId < Effect.values().length;
	}

	@Nullable
	public Effect getEffect()
	{
		return hasVanillaEffect ? Effect.values()[this.effectId] : null;
	}

	@Override
	public void serialize(ByteBuf buf)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean shouldRunOnGameThread()
	{
		return true;
	}
}

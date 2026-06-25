package micheal65536.fountain;

import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftTypes;
import org.geysermc.mcprotocollib.protocol.data.game.entity.Effect;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundRemoveMobEffectPacket;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * Custom packet that intercepts ClientboundRemoveMobEffectPacket to handle
 * effect IDs beyond the vanilla range (custom modded effects).
 */
public class ClientboundRemoveMobEffectCustomPacket implements MinecraftPacket
{
	public static ClientboundRemoveMobEffectCustomPacket read(ByteBuf in) 
	{
		int mark = in.readerIndex();
		int entityId = MinecraftTypes.readVarInt(in);
		int effectId = MinecraftTypes.readVarInt(in);

		if (effectId >= Effect.values().length)
		{
			return new ClientboundRemoveMobEffectCustomPacket(entityId, effectId, false);
		}
		else
		{
			in.readerIndex(mark);
			return null;
		}
	}

	public final int entityId;
	public final int effectId;
	private final boolean hasVanillaEffect;

	private ClientboundRemoveMobEffectCustomPacket(int entityId, int effectId, boolean hasVanillaEffect)
	{
		this.entityId = entityId;
		this.effectId = effectId;
		this.hasVanillaEffect = hasVanillaEffect;
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

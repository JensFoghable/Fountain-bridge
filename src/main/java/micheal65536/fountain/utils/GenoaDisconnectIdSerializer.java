package micheal65536.fountain.utils;

import io.netty.buffer.ByteBuf;
import org.cloudburstmc.protocol.bedrock.codec.v425_genoa.serializer.GenoaDisconnectIdSerializer_v425_Genoa;
import org.cloudburstmc.protocol.bedrock.packet.GenoaDisconnectIdPacket;

// the client reads seven stream versions, without the boost one
public final class GenoaDisconnectIdSerializer extends GenoaDisconnectIdSerializer_v425_Genoa
{
	public static final GenoaDisconnectIdSerializer INSTANCE = new GenoaDisconnectIdSerializer();

	@Override
	protected void readStreamIds(ByteBuf buffer, GenoaDisconnectIdPacket packet)
	{
		if (buffer.readBoolean())
		{
			packet.setBuildplatesStreamVersion(buffer.readLongLE());
		}
		if (buffer.readBoolean())
		{
			packet.setChallengesStreamVersion(buffer.readLongLE());
		}
		if (buffer.readBoolean())
		{
			packet.setProfilesStreamVersion(buffer.readLongLE());
		}
		if (buffer.readBoolean())
		{
			packet.setCraftingStreamVersion(buffer.readLongLE());
		}
		if (buffer.readBoolean())
		{
			packet.setInventoryStreamVersion(buffer.readLongLE());
		}
		if (buffer.readBoolean())
		{
			packet.setSmeltingStreamVersion(buffer.readLongLE());
		}
		if (buffer.readBoolean())
		{
			packet.setTokensStreamVersion(buffer.readLongLE());
		}
	}

	@Override
	protected void writeStreamIds(ByteBuf buffer, GenoaDisconnectIdPacket packet)
	{
		writeStreamVersion(buffer, packet.getBuildplatesStreamVersion());
		writeStreamVersion(buffer, packet.getChallengesStreamVersion());
		writeStreamVersion(buffer, packet.getProfilesStreamVersion());
		writeStreamVersion(buffer, packet.getCraftingStreamVersion());
		writeStreamVersion(buffer, packet.getInventoryStreamVersion());
		writeStreamVersion(buffer, packet.getSmeltingStreamVersion());
		writeStreamVersion(buffer, packet.getTokensStreamVersion());
	}

	private static void writeStreamVersion(ByteBuf buffer, long version)
	{
		buffer.writeBoolean(version != 0);
		if (version != 0)
		{
			buffer.writeLongLE(version);
		}
	}
}

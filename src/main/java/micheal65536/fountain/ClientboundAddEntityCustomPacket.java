package micheal65536.fountain;

import org.geysermc.mcprotocollib.protocol.codec.MinecraftPacket;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftTypes;
import org.geysermc.mcprotocollib.protocol.data.game.entity.type.EntityType;
import org.geysermc.mcprotocollib.protocol.data.game.entity.object.ObjectData;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.Nullable;

import io.netty.buffer.ByteBuf;
import org.cloudburstmc.math.vector.Vector3f;
import java.io.IOException;
import java.util.UUID;

/**
 * Custom packet that intercepts ClientboundAddEntityPacket to handle entity types
 * beyond the vanilla range (custom modded entities with IDs > vanilla max).
 * Only used server-side; never serialized back.
 */
public class ClientboundAddEntityCustomPacket implements MinecraftPacket
{
	public static ClientboundAddEntityCustomPacket read(ByteBuf in) 
	{
		// Sniff the entity type ID first to decide if it's custom
		int mark = in.readerIndex();
		int entityId = MinecraftTypes.readVarInt(in);
		UUID uuid = MinecraftTypes.readUUID(in);
		int typeId = MinecraftTypes.readVarInt(in);

		if (typeId < EntityType.values().length)
		{
			// Vanilla type: rewind, return null so regular deserialization can handle it
			in.readerIndex(mark);
			return null;
		}
		else
		{
			// Custom type: continue reading the rest of the data
			double x = in.readDouble();
			double y = in.readDouble();
			double z = in.readDouble();
			double motionX = in.readDouble();
			double motionY = in.readDouble();
			double motionZ = in.readDouble();
			float pitch = in.readFloat();
			float yaw = in.readFloat();
			float headYaw = in.readFloat();
			return new ClientboundAddEntityCustomPacket(entityId, uuid, typeId, x, y, z, motionX, motionY, motionZ, yaw, pitch, headYaw);
		}
	}

	public final int entityId;
	public final UUID uuid;
	public final int typeId;
	public final double x;
	public final double y;
	public final double z;
	public final double motionX;
	public final double motionY;
	public final double motionZ;
	public final float yaw;
	public final float pitch;
	public final float headYaw;
	public final boolean hasVanillaType;

	private ClientboundAddEntityCustomPacket(int entityId, UUID uuid, int typeId, double x, double y, double z, double motionX, double motionY, double motionZ, float yaw, float pitch, float headYaw)
	{
		this.entityId = entityId;
		this.uuid = uuid;
		this.typeId = typeId;
		this.x = x;
		this.y = y;
		this.z = z;
		this.motionX = motionX;
		this.motionY = motionY;
		this.motionZ = motionZ;
		this.yaw = yaw;
		this.pitch = pitch;
		this.headYaw = headYaw;
		this.hasVanillaType = typeId < EntityType.values().length;
	}

	@Nullable
	public EntityType getType()
	{
		return this.hasVanillaType ? EntityType.values()[this.typeId] : null;
	}

	public double getX() { return x; }
	public double getY() { return y; }
	public double getZ() { return z; }
	public double getMotionX() { return motionX; }
	public double getMotionY() { return motionY; }
	public double getMotionZ() { return motionZ; }
	public float getYaw() { return yaw; }
	public float getPitch() { return pitch; }
	public float getHeadYaw() { return headYaw; }
	public int getEntityId() { return entityId; }

	public Vector3f getMovement() { return Vector3f.from((float)motionX, (float)motionY, (float)motionZ); }

	/*
	 * Dummy getter for compatibility with EntityTranslator.createEntityInstance.
	 * Only valid when type is an actual vanilla EntityType (which means this packet
	 * would not exist in practice since read() returns null for vanilla types).
	 */
	public @org.jetbrains.annotations.Nullable ObjectData getData() { return null; }

	@Override
	public void serialize(ByteBuf buf)
	{
		// Server -> client only, never serialized back
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean shouldRunOnGameThread()
	{
		return true;
	}
}

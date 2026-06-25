package micheal65536.fountain.utils;

import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtMapBuilder;
import org.geysermc.mcprotocollib.protocol.data.game.item.ItemStack;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.DataComponent;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.DataComponentType;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.DataComponentTypes;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.DataComponents;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.IntComponentType;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.type.ObjectDataComponent;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.type.IntDataComponent;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper for reading/writing custom Earth data on ItemStack using the new
 * DataComponents API (MCProtocolLib 1.21.x).
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class ItemStackDataHelper
{
	public static String readGenoaInstanceId(ItemStack stack)
	{
		if (stack == null) return null;
		DataComponents comps = stack.getDataComponentsPatch();
		if (comps == null) return null;
		DataComponent component = comps.getDataComponents().get(DataComponentTypes.CUSTOM_DATA);
		if (component == null) return null;
		NbtMap customData = (NbtMap) component.getValue();
		if (customData != null && customData.containsKey("GenoaInstanceId"))
		{
			return customData.getString("GenoaInstanceId");
		}
		return null;
	}

	public static int readDamage(ItemStack stack)
	{
		if (stack == null) return 0;
		DataComponents comps = stack.getDataComponentsPatch();
		if (comps == null) return 0;
		DataComponent component = comps.getDataComponents().get(DataComponentTypes.DAMAGE);
		if (component == null) return 0;
		return (int) component.getValue();
	}

	public static ItemStack withGenoaData(int javaId, int count, String instanceId, int wear)
	{
		Map<DataComponentType<?>, DataComponent<?, ?>> map = new HashMap<>();

		NbtMapBuilder builder = NbtMap.builder();
		builder.put("GenoaInstanceId", instanceId);
		if (wear > 0)
		{
			builder.put("Damage", wear);
		}
		NbtMap customData = builder.build();

		DataComponentType<NbtMap> customDataType = DataComponentTypes.CUSTOM_DATA;
		map.put(customDataType, new ObjectDataComponent<>(customDataType, customData));

		if (wear > 0)
		{
			IntComponentType damageType = DataComponentTypes.DAMAGE;
			map.put(damageType, new IntDataComponent(damageType, wear));
		}

		DataComponents comps = new DataComponents(map);
		return new ItemStack(javaId, count, comps);
	}
}

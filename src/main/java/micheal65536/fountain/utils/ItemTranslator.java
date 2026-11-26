package micheal65536.fountain.utils;

import com.github.steveice10.mc.protocol.data.game.entity.metadata.ItemStack;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import org.apache.logging.log4j.LogManager;
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import micheal65536.fountain.Main;
import micheal65536.fountain.registry.BedrockItems;
import micheal65536.fountain.registry.EarthItemCatalog;
import micheal65536.fountain.registry.JavaItems;

public final class ItemTranslator
{
	@NotNull
	public static ItemData translateJavaToBedrock(@Nullable ItemStack itemStack, @NotNull FabricRegistryManager fabricRegistryManager)
	{
		ItemData.Builder builder = ItemData.builder();

		if (itemStack == null)
		{
			return builder.build();
		}

		int javaId = itemStack.getId();
		JavaItems.BedrockMapping bedrockMapping = JavaItems.getBedrockMapping(javaId, fabricRegistryManager);
		if (bedrockMapping == null)
		{
			LogManager.getLogger().warn("Attempt to translate item with no mapping {}", JavaItems.getName(javaId, fabricRegistryManager));
			return builder.build();
		}
		builder.definition(Main.ITEM_DEFINITION_REGISTRY.getDefinition(bedrockMapping.id));

		if (bedrockMapping.toolWear)
		{
			CompoundTag nbt = itemStack.getNbt();
			int damage = nbt != null && nbt.contains("Damage") ? (int) nbt.get("Damage").getValue() : 0;
			builder.damage(toEarthWear(bedrockMapping, JavaItems.getName(javaId, fabricRegistryManager), damage));
		}
		else
		{
			builder.damage(bedrockMapping.aux);
		}

		builder.count(itemStack.getAmount());

		return builder.build();
	}

	private static int toEarthWear(@NotNull JavaItems.BedrockMapping bedrockMapping, @Nullable String javaName, int javaDamage)
	{
		if (javaDamage <= 0)
		{
			return 0;
		}

		String bedrockName = BedrockItems.getName(bedrockMapping.id);
		if (bedrockName == null)
		{
			return javaDamage;
		}

		String uuid = EarthItemCatalog.getUUID(bedrockName, bedrockMapping.aux);
		if (uuid == null)
		{
			return javaDamage;
		}

		EarthItemCatalog.ItemInfo itemInfo = EarthItemCatalog.getItemInfo(uuid);
		if (itemInfo == null || itemInfo.maxWear <= 0)
		{
			return javaDamage;
		}

		return ToolDurability.toEarthWear(javaDamage, itemInfo.maxWear, javaName != null ? javaName : bedrockName);
	}
}
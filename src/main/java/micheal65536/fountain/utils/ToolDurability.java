package micheal65536.fountain.utils;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public final class ToolDurability
{
	private static final Map<String, Integer> JAVA_MAX_DAMAGE = new HashMap<>();

	static
	{
		put(59, "wooden_sword", "wooden_shovel", "wooden_pickaxe", "wooden_axe", "wooden_hoe");
		put(131, "stone_sword", "stone_shovel", "stone_pickaxe", "stone_axe", "stone_hoe");
		put(250, "iron_sword", "iron_shovel", "iron_pickaxe", "iron_axe", "iron_hoe");
		put(32, "golden_sword", "golden_shovel", "golden_pickaxe", "golden_axe", "golden_hoe");
		put(1561, "diamond_sword", "diamond_shovel", "diamond_pickaxe", "diamond_axe", "diamond_hoe");
		put(2031, "netherite_sword", "netherite_shovel", "netherite_pickaxe", "netherite_axe", "netherite_hoe");

		put(238, "shears");
		put(384, "bow");
		put(465, "crossbow");
		put(64, "fishing_rod", "flint_and_steel");
		put(25, "carrot_on_a_stick");
		put(250, "trident");
		put(336, "shield");
		put(432, "elytra");

		put(55, "leather_helmet");
		put(80, "leather_chestplate");
		put(75, "leather_leggings");
		put(65, "leather_boots");
		put(165, "chainmail_helmet", "iron_helmet");
		put(240, "chainmail_chestplate", "iron_chestplate");
		put(225, "chainmail_leggings", "iron_leggings");
		put(195, "chainmail_boots", "iron_boots");
		put(77, "golden_helmet");
		put(112, "golden_chestplate");
		put(105, "golden_leggings");
		put(91, "golden_boots");
		put(363, "diamond_helmet");
		put(528, "diamond_chestplate");
		put(495, "diamond_leggings");
		put(429, "diamond_boots");
	}

	private static void put(int durability, String... names)
	{
		for (String name : names)
		{
			JAVA_MAX_DAMAGE.put("minecraft:" + name, durability);
		}
	}

	private ToolDurability()
	{
	}

	public static int javaMaxDamage(@NotNull String javaItemName)
	{
		return JAVA_MAX_DAMAGE.getOrDefault(javaItemName, 0);
	}

	public static int toEarthWear(int javaDamage, int earthMaxWear, @NotNull String javaItemName)
	{
		int javaMax = javaMaxDamage(javaItemName);
		if (javaMax <= 0 || earthMaxWear <= 0 || javaDamage <= 0)
		{
			return javaDamage;
		}

		long scaled = ((long) javaDamage * earthMaxWear + javaMax - 1) / javaMax;
		return (int) Math.min(scaled, earthMaxWear);
	}

	public static int toJavaDamage(int earthWear, int earthMaxWear, @NotNull String javaItemName)
	{
		int javaMax = javaMaxDamage(javaItemName);
		if (javaMax <= 0 || earthMaxWear <= 0 || earthWear <= 0)
		{
			return earthWear;
		}

		long scaled = (long) earthWear * javaMax / earthMaxWear;
		return (int) Math.min(scaled, javaMax - 1);
	}
}

package fluke.hexlands.config;

import fluke.hexlands.util.Reference;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = Reference.MOD_ID, category = "")
@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class Configs {

	public static ConfigWorldGen worldgen = new ConfigWorldGen();
	public static ConfigDimension dimension = new ConfigDimension();
	public static ConfigNether nether = new ConfigNether();

	public static class ConfigWorldGen 
	{
		@Config.Comment({"Controls size of hex tiles. Larger number = Bigger hex", "Default: 36"})
		@Config.RequiresWorldRestart
		public int hexSize = 36;
		@Config.Comment({"Size of biomes. Lower values = Larger biomes", "Default: 600"})
		@Config.RequiresWorldRestart
		public int biomeSize = 600;
		@Config.Comment({"Height (y-level) terrain is adjusted by (aka extra terrain noise inside the hex)", "Default: 78"})
		@Config.RequiresWorldRestart
		public int terrainHeight = 78;
		@Config.Comment({"Height (y-level) terrain is before adjustments", "Default: 66"})
		@Config.RequiresWorldRestart
		public int terrainBaseline = 66;
		@Config.Comment({"How often lakes generate. Lower numbers = More lakes", "Default: 6"})
		@Config.RequiresWorldRestart
		public int lakeRarity = 6;
		@Config.Comment({"Height (y-level) of oceans", "Default: 60"})
		@Config.RequiresWorldRestart
		public int seaLevel = 60;
		@Config.Comment({"How much biomes influence the final height of the hex. Higher numbers push biomes away from the terrainBaseline", "Default: 18"})
		@Config.RequiresWorldRestart
		public int biomeHeightAdjustment = 18;
		@Config.Comment({"Extra height (y-level) to adjust each hex by, best used with outlineAll setting", "Default: 0"})
		@Config.RequiresWorldRestart
		public int extraHexNoise = 0;
		@Config.Comment({"How many attempts per chunk to generate dungeons. Higher numbers = More dungeons", "Default: 8"})
		@Config.RequiresWorldRestart
		public int dungeonCount = 8;
		@Config.Comment({"How much lower to adjust ocean and deep ocean biomes", "Default: 16"})
		@Config.RequiresWorldRestart
		public int oceanHeight = 16;
		@Config.Comment({"Biomes where the border should be 1 lower (makes getting out of water easier). If empty all biomes borders will be 1 lower", "Default: minecraft:ocean, minecraft:deep_ocean, minecraft:river, biomesoplenty:kelp_forest, biomesoplenty:coral_reef"})
		@Config.RequiresWorldRestart
		public String[] sunkenBiomes = {"minecraft:ocean", "minecraft:deep_ocean", "minecraft:river", "biomesoplenty:kelp_forest", "biomesoplenty:coral_reef"};
		@Config.Comment({"Draw borders around every hex", "Default: false"})
		@Config.RequiresWorldRestart
		public boolean outlineAll = false;
		@Config.Comment({"Draw borders around every  DRY hex (biomes other than those listed in wetBiomes)", "Default: false"})
		@Config.RequiresWorldRestart
		public boolean outlineAllDry = false;
		@Config.Comment({"Defines which biomes are ocean types. Used by oceanHeight and outlineAllDry config settings", "Default: minecraft:ocean, minecraft:deep_ocean, biomesoplenty:kelp_forest, biomesoplenty:coral_reef"})
		@Config.RequiresWorldRestart
		public String[] wetBiomes = {"minecraft:ocean", "minecraft:deep_ocean", "biomesoplenty:kelp_forest", "biomesoplenty:coral_reef"};
		@Config.Comment({"Master command for generating all vanilla structures: mineshaft, village, stronghold, temples, etc", "Default: true"})
		@Config.RequiresWorldRestart
		public boolean generateStructures = true;
		@Config.Comment({"Generate mineshaft", "Default: true"})
		@Config.RequiresWorldRestart
		public boolean generateMineshaft = true;
		@Config.Comment({"Generate temples", "Default: true"})
		@Config.RequiresWorldRestart
		public boolean generateTemples = true;
		@Config.Comment({"Generate villages", "Default: true"})
		@Config.RequiresWorldRestart
		public boolean generateVillage = true;
		@Config.Comment({"Generate stronghold", "Default: true"})
		@Config.RequiresWorldRestart
		public boolean generateStronghold = true;
		@Config.Comment({"Generate mansions", "Default: true"})
		@Config.RequiresWorldRestart
		public boolean generateMansions = true;
		@Config.Comment({"Generate ocean monuments", "Default: true"})
		@Config.RequiresWorldRestart
		public boolean generateMonuments = true;
		@Config.Comment({"Generate caves", "Default: true"})
		@Config.RequiresWorldRestart
		public boolean generateCaves = true;
		@Config.Comment({"Generate ravines", "Default: true"})
		@Config.RequiresWorldRestart
		public boolean generateRavines = true;
		@Config.Comment({"Generate border block down to bedrock rather than just at the surface", "Default: false"})
		@Config.RequiresWorldRestart
		public boolean borderToBedrock = false;
		@Config.Comment({"Should water lakes generate", "Default: true"})
		@Config.RequiresWorldRestart
		public boolean lakesGenerate = true;
		@Config.Comment({"Should lava lakes generate above y=10", "Default: true"})
		@Config.RequiresWorldRestart
		public boolean lavaLakesGenerate = true;
		@Config.RequiresWorldRestart
		@Config.Comment({"What block to use for dividing the grid, use @ for metadata", "Example: minecraft:concrete@6", "Default: minecraft:stonebrick"})
		public String rimBlock = "minecraft:stonebrick";
		@Config.Comment({"How much taller than normal should the rim blocks be. Range: -256 to 256", "Default: 0"})
		@Config.RequiresWorldRestart
		public int extraRimHeight = 0;
		@Config.Comment({"Controls how thick the hex border is. Works better with odd numbers", "Default: 3"})
		@Config.RequiresWorldRestart
		public int rimSize = 3;
		
	}
	
	public static class ConfigDimension 
	{
		@Config.Comment({"Generate new dimension with hex-land generation", "Note: no method exists to travel to this dimension and must be added by the pack maker", "Default: false"})
		@Config.RequiresWorldRestart
		public boolean generateDim = false;
		@Config.Comment({"Force overworld hex generation even when world type is not selected", "Default: false"})
		@Config.RequiresWorldRestart
		public boolean forceHexGen = false;
		
		@Config.Comment({"What dimension ID to use", "Default: 88"})
		@Config.RequiresWorldRestart
		public int dimID = 88;
		
		@Config.RequiresWorldRestart
		@Config.Comment({"What world type to use if forceHexGen is enabled. Must be hexlands or bophex",  "Default: hexlands"})
		public String forcedWorldType = "hexlands";
	}
	
	public static class ConfigNether
	{
		@Config.Comment({"Use hex generation in the nether", "Default: false"})
		@Config.RequiresWorldRestart
		public boolean useNetherHexGen = false;
		
		@Config.RequiresWorldRestart
		@Config.Comment({"What block to use for dividing the grid in the nether, use @ for metadata", "Example: minecraft:concrete@6", "Default: minecraft:nether_wart_block"})
		public String netherRimBlock = "minecraft:nether_wart_block";
		
		@Config.RequiresWorldRestart
		@Config.Comment({"Extend rim blocks to bedrock for lower hexes and covers sides of midland hexes", "Default: false"})
		public boolean netherExtendedRimBlock = false;
		
		@Config.RequiresWorldRestart
		@Config.Comment({"Use BoP nether biomes if BoP is installed", "Default: true"})
		public boolean netherUseBoPBiomes = true;
		
		@Config.Comment({"Percent chance top layer of hex is made of soul sand (between 0.0 and 1.0)", "Default: 0.14"})
		@Config.RequiresWorldRestart
		public double souldHexChance = 0.14;
		
		@Config.Comment({"Sea level for nether lava", "Default: 31"})
		@Config.RequiresWorldRestart
		public int netherSeaLevel = 31;
		
		@Config.Comment({"Controls how thick the hex border is. Works better with odd numbers", "Default: 3"})
		@Config.RequiresWorldRestart
		public int netherRimSize = 3;
	
	}

	@SubscribeEvent
	public static void onConfigReload(ConfigChangedEvent.OnConfigChangedEvent event) 
	{
		if (Reference.MOD_ID.equals(event.getModID()))
			ConfigManager.sync(Reference.MOD_ID, Config.Type.INSTANCE);
	}
}


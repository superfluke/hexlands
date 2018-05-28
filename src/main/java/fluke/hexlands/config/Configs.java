package fluke.hexlands.config;

import java.io.File;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class Configs {
	
	public static Configuration config;
	public static int hexWidth, hexHeight, biomeSize, terrainHeight, terrainBaseline, lakeRarity;
	public static boolean outlineAll, generateStructures, generateCaves;
	public static String rimBlock;
	public static final String WORLD_CONFIG = "worldgen";

	public static void loadFromFile(File configFile)
	{
		config = new Configuration(configFile);
		config.load();
		loadConfigs(config);
	}
	
	public static void loadConfigs(Configuration c)
	{
		Property prop;
		
		prop = c.get(WORLD_CONFIG, "hexWidth", 36);
        prop.setComment("width (x-axis) of hex tiles (Default: 36)");
        hexWidth = prop.getInt();
        
        prop = c.get(WORLD_CONFIG, "hexHeight", 36);
        prop.setComment("height (z-axis) of hex tiles (Default: 36)");
        hexHeight = prop.getInt();
        
        prop = c.get(WORLD_CONFIG, "biomeSize", 600);
        prop.setComment("size of biomes, lower values = larger biomes (Default: 600)");
        biomeSize = prop.getInt();
        
        prop = c.get(WORLD_CONFIG, "outlineAll", false);
        prop.setComment("draw borders around every hex (Default: false)");
        outlineAll = prop.getBoolean();
        
        prop = c.get(WORLD_CONFIG, "generateStructures", true);
        prop.setComment("generate vanilla structures: mineshaft, village, stronghold, temples... (Default: true)");
        generateStructures = prop.getBoolean();
        
        prop = c.get(WORLD_CONFIG, "generateCaves", true);
        prop.setComment("generate caves and ravines (Default: true)");
        generateCaves = prop.getBoolean();
        
        prop = c.get(WORLD_CONFIG, "terrainBaseline", 68);
        prop.setComment("height (y-level) terrain is before adjustments (Default: 68)");
        terrainBaseline = prop.getInt();
        
        prop = c.get(WORLD_CONFIG, "terrainHeight", 16);
        prop.setComment("height (y-level) terrain is adjusted by (Default: 16)");
        terrainHeight = prop.getInt();
        
        prop = c.get(WORLD_CONFIG, "lakeRarity", 5);
        prop.setComment("how often lakes generate, lower numbers = more lakes (Default: 5)");
        lakeRarity = prop.getInt();
        
        prop = c.get(WORLD_CONFIG, "rimBlock", "minecraft:stonebrick");
        prop.setComment("what block to use for dividing the grid (Default: minecraft:stonebrick)");
        rimBlock = prop.getString();
        
        c.save();
	}

}

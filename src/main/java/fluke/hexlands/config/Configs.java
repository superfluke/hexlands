package fluke.hexlands.config;

import java.io.File;

import fluke.hexlands.world.ChunkGeneratorOverworldHex;

import net.minecraft.init.Blocks;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class Configs {
	
	public static Configuration config;
	public static int hexSize, biomeSize, terrainHeight, terrainBaseline, lakeRarity, seaLevel, biomeHeightAdjustment, extraHexNoise;
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
		
		prop = c.get(WORLD_CONFIG, "hexSize", 36);
        prop.setComment("controls size of hex tiles, larger number = bigger hex (Default: 36)");
        hexSize = prop.getInt();
        
        /*
        prop = c.get(WORLD_CONFIG, "hexHeight", 36);
        prop.setComment("height (z-axis) of hex tiles (Default: 36)");
        hexHeight = prop.getInt();
        */
        
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
        
        prop = c.get(WORLD_CONFIG, "terrainBaseline", 66);
        prop.setComment("height (y-level) terrain is before adjustments (Default: 66)");
        terrainBaseline = prop.getInt();
        
        prop = c.get(WORLD_CONFIG, "terrainHeight", 78);
        prop.setComment("height (y-level) terrain is adjusted by (Default: 78)");
        terrainHeight = prop.getInt();
        
        prop = c.get(WORLD_CONFIG, "biomeHeightAdjustment", 18);
        prop.setComment("how much biomes influence the final height of the hex (Default: 18)");
        biomeHeightAdjustment = prop.getInt();
        
        prop = c.get(WORLD_CONFIG, "extraHexNoise", 0);
        prop.setComment("extra height (y-level) to adjust each hex by, best used with outlineAll setting (Default: 0)");
        extraHexNoise = prop.getInt();
        
        prop = c.get(WORLD_CONFIG, "seaLevel", 60);
        prop.setComment("height (y-level) of oceans (Default: 60)");
        seaLevel = prop.getInt();
        
        prop = c.get(WORLD_CONFIG, "lakeRarity", 6);
        prop.setComment("how often lakes generate, lower numbers = more lakes (Default: 6)");
        lakeRarity = prop.getInt();
        
        prop = c.get(WORLD_CONFIG, "rimBlock", "minecraft:stonebrick");
        prop.setComment("what block to use for dividing the grid (Default: minecraft:stonebrick)");
        rimBlock = prop.getString();
        
        c.save();
	}

}


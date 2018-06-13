//package fluke.hexlands.world;
//
//import java.util.logging.Level;
//
//import org.apache.commons.lang3.ArrayUtils;
//
//import net.minecraft.init.Biomes;
//import net.minecraft.util.ResourceLocation;
//import net.minecraft.world.biome.Biome;
//import net.minecraftforge.common.BiomeManager;
//import net.minecraftforge.common.BiomeManager.BiomeType;
//import fluke.hexlands.Main;
//import fluke.hexlands.config.Configs;
//
//public class BiomeEditor {
//	public static Biome[] bl = new Biome[Configs.worldgen.biomeBlacklist.length];// {Biome.REGISTRY.getObject(new ResourceLocation("minecraft:forest"))};
//	
//	//public static String[] bl = {"minecraft:forest"};
//	public static void removeBlacklistBiomes()
//	{
//		System.out.println(Biome.REGISTRY.getKeys());
//		//System.out.println(bl[0].toString());
//		
//		for (int i=0; i<Configs.worldgen.biomeBlacklist.length; i++)
//		{
//			bl[i] = Biome.REGISTRY.getObject(new ResourceLocation(Configs.worldgen.biomeBlacklist[i]));
//			System.out.println(bl[i].getBiomeName());
//		}
//		Biome[] heyo = {Biomes.BEACH, Biomes.TAIGA, Biomes.SWAMPLAND, Biomes.BIRCH_FOREST};
//		for (BiomeManager.BiomeType type : BiomeManager.BiomeType.values()) 
//		{
//			for (BiomeManager.BiomeEntry entry : BiomeManager.getBiomes(type)) 
//			{
//				System.out.println(entry.biome.getBiomeName());
//				
//				if(ArrayUtils.contains(heyo, entry.biome)) // this works fine
//				if(ArrayUtils.contains(bl, entry.biome)) //this doesnt do shit
//				{
//					Main.LOGGER.info("Removing biome from generation: " + entry.biome.getRegistryName());
//					BiomeManager.removeBiome(type, entry);
//				
//				}
//				//if (entry.biome == Biomes.PLAINS || entry.biome == Biomes.SWAMPLAND)
//					
//			}
//		}
//	}
//
//}

package fluke.hexlands.world;

import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.common.BiomeManager.BiomeType;

public class biomeEditor {
	//public Biome[] bl = {Biome.REGISTRY.get};
	public static void removeBlacklistBiomes()
	{
		System.out.println(Biome.REGISTRY.getKeys().toString());
		for (BiomeManager.BiomeType type : BiomeManager.BiomeType.values()) 
		{
			for (BiomeManager.BiomeEntry entry : BiomeManager.getBiomes(type)) 
			{
				System.out.println(entry.biome.getBiomeName());
				if (entry.biome == Biomes.PLAINS || entry.biome == Biomes.SWAMPLAND)
					BiomeManager.removeBiome(type, entry);
			}
		}
	}

}

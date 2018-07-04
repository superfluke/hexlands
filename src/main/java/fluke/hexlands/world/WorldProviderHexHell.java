package fluke.hexlands.world;

import biomesoplenty.common.world.BiomeProviderBOPHell;
import fluke.hexlands.config.Configs;
import net.minecraft.init.Biomes;
import net.minecraft.world.WorldProviderHell;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.Loader;

public class WorldProviderHexHell extends WorldProviderHell 
{
	
	@Override
	public void init()
    {
		if (Loader.isModLoaded("biomesoplenty") && Configs.nether.netherUseBoPBiomes)
		{
			this.biomeProvider = new HexHellBoPBiomeProvider(this.world);
		}
		else
		{
			this.biomeProvider = new BiomeProviderSingle(Biomes.HELL);
		}
        this.doesWaterVaporize = true;
        this.nether = true;
    }
	
	@Override
    public IChunkGenerator createChunkGenerator()
    {
        return new ChunkGeneratorHellHex(this.world, this.world.getWorldInfo().isMapFeaturesEnabled(), this.world.getSeed());
    }

}

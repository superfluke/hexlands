package fluke.hexlands.world;

import net.minecraft.init.Biomes;
import net.minecraft.world.WorldProviderHell;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.gen.IChunkGenerator;

public class WorldProviderHexHell extends WorldProviderHell 
{
	
	@Override
	public void init()
    {
        this.biomeProvider = new BiomeProviderSingle(Biomes.HELL);
        this.doesWaterVaporize = true;
        this.nether = true;
    }
	
	@Override
    public IChunkGenerator createChunkGenerator()
    {
        return new ChunkGeneratorHellHex(this.world, this.world.getWorldInfo().isMapFeaturesEnabled(), this.world.getSeed());
    }

}

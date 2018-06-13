package fluke.hexlands.world;

import fluke.hexlands.Main;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.gen.IChunkGenerator;

public class WorldProviderHex extends WorldProvider
{
	@Override
	protected void init()
    {
        this.hasSkyLight = true;
        this.biomeProvider =  new HexBiomeProvider(world.getWorldInfo());
    }
	
	
	public IChunkGenerator createChunkGenerator()
    {
        return new ChunkGeneratorOverworldHex(world);
    }
	
	@Override
	public DimensionType getDimensionType() 
	{
		return Main.HEX_DIM;
	}
	
	@Override
    public boolean isSurfaceWorld()
    {
        return true;
    }
	
	@Override
    public boolean canDoLightning(net.minecraft.world.chunk.Chunk chunk)
    {
        return true;
    }
	
	

}

package fluke.hexlands.world;

import net.minecraft.init.Biomes;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class WorldTypeFluke extends WorldType
{
	
	public WorldTypeFluke()
	{
		super("hexlands");
	}
	
	@Override
	public IChunkGenerator getChunkGenerator(World world, String genOptions)
	{
		return new ChunkGeneratorOverworldHex(world);
	}
	
	@Override
    public boolean isCustomizable()
    {
        return false;
    }
	
	public net.minecraft.world.biome.BiomeProvider getBiomeProvider(World world)
    {
        return new HexBiomeProvider(world.getWorldInfo(), Biomes.DESERT);        
    }
	
	@SideOnly(Side.CLIENT)
    public String getTranslationKey()
    {
        return "Hex Lands";
    }
}

package fluke.hexlands.world;

import java.util.Random;

import fluke.hexlands.config.Configs;
import fluke.hexlands.util.hex.Hex;
import fluke.hexlands.util.hex.Layout;
import fluke.hexlands.util.hex.Point;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class WorldHandler implements IWorldGenerator
{
	
	public static final int OFFSET = 8;
    protected Layout hex_layout = new Layout(Layout.flat, new Point(Configs.worldgen.hexSize, Configs.worldgen.hexSize), new Point(0, 0));

//	@EventHandler
//	public void preInit(FMLPreInitializationEvent e) 
//	{
//		GameRegistry.registerWorldGenerator(new WorldHandler(), 9999999);;
//	}
	
	@Override
	public void generate(Random rand, int chunkX, int chunkZ, World world, IChunkGenerator generator, IChunkProvider provider) 
	{
		
		if (world.getWorldInfo().getTerrainType() == WorldType.FLAT || Configs.worldgen.missingHexChance == 0) 
		{
			return;
		}
		
		int dimID = world.provider.getDimension();
		if(!(dimID == 0 || dimID == Configs.dimension.dimID))
		{
			return;
		}
		
		deleteBedrock(chunkX, chunkZ, world, rand);
	}
	
	public void deleteBedrock(int chunkX, int chunkZ, World world, Random rand)
    {
    	for (int x=0; x<16; x++)
    	{
    		int realX = x + chunkX*16 + OFFSET;
    		for (int z=0; z<16; z++)
    		{
    			int realZ = z + chunkZ*16 + OFFSET;
    			Hex hexy = hex_layout.pixelToHex(new Point(realX, realZ)).hexRound();
    			if(Hex.isHexVoid(world.getSeed(), rand, hexy.q, hexy.r)) //TODO cache hex and isVoid here
    			{
	    			for (int y=0; y<6; y++)
	    			{
	    				BlockPos position = new BlockPos(realX, y, realZ);
	    				IBlockState state = world.getBlockState(position);
	    				if (!state.getBlock().isAssociatedBlock(Blocks.AIR)) {
							world.setBlockState(position, Blocks.AIR.getDefaultState(), 2);
						}
	    			}
    			}
    		}
    	}
    }
}

package fluke.hexlands.world;

import fluke.hexlands.Main;
import fluke.hexlands.config.Configs;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldProvider.WorldSleepResult;
import net.minecraft.world.end.DragonFightManager;
import net.minecraft.world.gen.IChunkGenerator;

public class WorldProviderHex extends WorldProvider
{
	private long worldTime = 0;
	
	@Override
	protected void init()
    {
        this.hasSkyLight = true;
        this.biomeProvider =  new HexBiomeProvider(world.getWorldInfo());
        NBTTagCompound nbttagcompound = this.world.getWorldInfo().getDimensionData(Configs.dimension.dimID);
        this.worldTime = this.world instanceof WorldServer ? nbttagcompound.getLong("HexTime") : 0;
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
	
	@Override
	public WorldSleepResult canSleepAt(net.minecraft.entity.player.EntityPlayer player, BlockPos pos)
    {
        return WorldSleepResult.ALLOW;
    }
	
	@Override
	public long getWorldTime()
    {
        return this.worldTime;
    }
	
	@Override
	public void setWorldTime(long time)
    {
        this.worldTime = time;
    }
	
	@Override
	public void onWorldSave()
    {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        nbttagcompound.setLong("HexTime", this.worldTime);
        this.world.getWorldInfo().setDimensionData(Configs.dimension.dimID, nbttagcompound);
    }

}

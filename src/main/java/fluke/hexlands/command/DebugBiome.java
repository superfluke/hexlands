package fluke.hexlands.command;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import fluke.hexlands.util.Reference;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Biomes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.biome.Biome;

public class DebugBiome extends CommandBase 
{
	private final List<String> aliases;
	
	public DebugBiome()
	{
        aliases = Lists.newArrayList(Reference.MOD_ID, "thisbiome", "tbio");
    }
	
	@Override
    @Nonnull
    public String getName() 
	{
        return "thisbiome";
    }
	
	@Override
    @Nonnull
    public List<String> getAliases() 
    {
        return aliases;
    }
	
	@Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args)
    {
        if (sender instanceof EntityPlayer) 
        {
        	double x = ((EntityPlayer) sender).posX;
        	double z = ((EntityPlayer) sender).posZ;
        	//String biome = server.worlds[0].getBiomeProvider().getBiome(new BlockPos(((EntityPlayer) sender).posX, 0D, ((EntityPlayer) sender).posZ)).getBiomeName();
        	Biome[] biomeList = server.worlds[0].getBiomeProvider().getBiomes(null, (int)x, (int)z, 3, 3);
        	 for (int k1 = 0; k1 < biomeList.length; ++k1)
             {
        		 sender.sendMessage(new TextComponentString("Biome: " + biomeList[k1]));
             }
        	 
        	   
        	 final List<Biome> WATER_BIOMES = Arrays.<Biome>asList(Biomes.DEEP_OCEAN);
        	 boolean flag = server.worlds[0].getBiomeProvider().areBiomesViable((int)x/16, (int)z/16, 3, WATER_BIOMES);
        	 sender.sendMessage(new TextComponentString("Water Biomes: " + flag));
        }
    }
	
	@Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) 
	{
        return true;
    }
	
	@Override
    @Nonnull
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) 
    {
        return Collections.emptyList();
    }

	@Override
	public String getUsage(ICommandSender sender) 
	{
		return "thisbiome";
	}

}

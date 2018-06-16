package fluke.hexlands.command;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import fluke.hexlands.util.Reference;
import fluke.hexlands.world.VanillaBiomeProvider;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Biomes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.biome.Biome;

public class SomeOtherShittyDebugCommand extends CommandBase 
{
	private final List<String> aliases;
	
	public SomeOtherShittyDebugCommand()
	{
        aliases = Lists.newArrayList(Reference.MOD_ID, "dbug");
    }
	
	@Override
    @Nonnull
    public String getName() 
	{
        return "dbug";
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
//        	List<Biome> water_biomes = Arrays.<Biome>asList(Biomes.OCEAN, Biomes.DEEP_OCEAN, Biomes.RIVER);
//        	//List<Biome> test = Arrays.<Biome>asList(Biomes.RIVER);
//        	Biome testo = Biomes.DESERT;
//        	boolean flag = water_biomes.contains(testo);
//        	System.out.println(flag);
//        	 //sender.sendMessage(new TextComponentString("Water Biomes: " + flag));
        	
        	Biome[] blist = ((VanillaBiomeProvider) server.worlds[0].getBiomeProvider()).viableList((int)x, (int)z, 6);
        	for (int i=0; i<blist.length; i++)
        	{
        		sender.sendMessage(new TextComponentString(blist[i].getBiomeName()));
        	}
        	
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
		return "dbug";
	}

}

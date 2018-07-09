package fluke.hexlands;

import java.util.Arrays;

import fluke.hexlands.config.Configs;
import fluke.hexlands.proxy.CommonProxy;
import fluke.hexlands.world.WorldTypeHexlands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.PropertyManager;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;


public class HexServer
{
	public static final String[] SERVER_SETTINGS = new String[] { "q", "field_71340_o", "settings" };	
	
	public void init() 
	{
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		if (server instanceof DedicatedServer) 
		{
			DedicatedServer dedi = (DedicatedServer) server;
			PropertyManager manager = ReflectionHelper.getPrivateValue(DedicatedServer.class, dedi, SERVER_SETTINGS);
			manager.setProperty("level-type", Configs.dimension.forcedWorldType);
			manager.saveProperties();
		}
		else
		{
			//look at me, I'm the world type now
			WorldType.WORLD_TYPES = new WorldTypeHexlands[]{new WorldTypeHexlands()};

		}
	}
}
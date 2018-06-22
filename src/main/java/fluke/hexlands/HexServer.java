package fluke.hexlands;

import fluke.hexlands.config.Configs;
import fluke.hexlands.proxy.CommonProxy;
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


public class HexServer extends CommonProxy
{
	public static final String[] SERVER_SETTINGS = new String[] { "q", "field_71340_o", "settings" };
	public static final String[] INTEGRATED_WORLD_SETTINGS = new String[] {"m", "field_71350_m", "worldSettings"};
	public static final String[] INTEGRATED_WORLD_TYPE = new String[] {"e", "field_77171_e", "terrainType"};
	
	@Override
	public void init(FMLInitializationEvent event) 
	{
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		if (server instanceof DedicatedServer) 
		{
			DedicatedServer dedi = (DedicatedServer) server;
			PropertyManager manager = ReflectionHelper.getPrivateValue(DedicatedServer.class, dedi, SERVER_SETTINGS);
			manager.setProperty("level-type", Configs.dimension.forcedWorldType);
			manager.saveProperties();
		}
	}
	
	@Override
	public void serverAboutToStart(FMLServerAboutToStartEvent event) 
	{
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		if(server instanceof IntegratedServer)
		{
			IntegratedServer innie = (IntegratedServer) server;
			WorldSettings worldSettings = ReflectionHelper.getPrivateValue(IntegratedServer.class, innie, INTEGRATED_WORLD_SETTINGS);
			WorldType lameWorld = ReflectionHelper.getPrivateValue(WorldSettings.class, worldSettings, INTEGRATED_WORLD_TYPE);
			System.out.println("hi mom");
			System.out.println(lameWorld.getName());
		}
	}
}
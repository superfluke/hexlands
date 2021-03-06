package fluke.hexlands.proxy;

import fluke.hexlands.HexServer;
import fluke.hexlands.config.Configs;
import fluke.hexlands.world.WorldProviderHexHell;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;

public class CommonProxy 
{
	public HexServer server;
	
	public void init(FMLInitializationEvent event) 
	{
		if(Configs.nether.useNetherHexGen)
			overrideNether();
		if(Configs.dimension.forceHexGen)
		{
			server = new HexServer();
			server.init();
		}
	}
	
	public void serverAboutToStart(FMLServerAboutToStartEvent event) 
	{
//		server.serverAboutToStart();
	}
	
	public void overrideNether()
	{
    	DimensionManager.unregisterDimension(-1);
        DimensionType netherHex = DimensionType.register("Nether", "_nether", -1, WorldProviderHexHell.class, false);
        DimensionManager.registerDimension(-1, netherHex);
    }

}

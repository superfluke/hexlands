package fluke.hexlands.proxy;

import fluke.hexlands.HexServer;
import fluke.hexlands.config.Configs;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;

public class CommonProxy 
{
	public HexServer server;
	
	public void init(FMLInitializationEvent event) 
	{
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

}

package fluke.hexlands.proxy;

import fluke.hexlands.HexServer;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;

public class CommonProxy 
{
	public HexServer server;
	
	public void init(FMLInitializationEvent event) 
	{
		//server = new HexServer();
		//server.init();
	}
	
	public void serverAboutToStart(FMLServerAboutToStartEvent event) 
	{
		//server.serverAboutToStart();
	}

}

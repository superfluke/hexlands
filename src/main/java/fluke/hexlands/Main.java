package fluke.hexlands;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.world.DimensionType;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import fluke.hexlands.command.DebugBiome;
import fluke.hexlands.command.SomeOtherShittyDebugCommand;
import fluke.hexlands.config.Configs;
import fluke.hexlands.proxy.CommonProxy;
import fluke.hexlands.util.Reference;
import fluke.hexlands.world.WorldProviderHex;
import fluke.hexlands.world.WorldTypeATest;
import fluke.hexlands.world.WorldTypeFluke;
//import fluke.hexlands.world.BiomeEditor;

@Mod(modid = Reference.MOD_ID, name = Reference.NAME, version = Reference.VERSION, acceptableRemoteVersions="*")
public class Main 
{
	public static WorldTypeFluke worldTypeFluke;
	public static WorldTypeATest aTest;
	public static final Logger LOGGER = LogManager.getLogger(Reference.MOD_ID);
	public static final DimensionType HEX_DIM = DimensionType.register("hex", "_lands", Configs.dimension.dimID, WorldProviderHex.class, false);
	public static final int OVERWORLD_ID = 0;
	
	@Instance
	public static Main instance;
	
	@SidedProxy(clientSide = Reference.CLIENT_PROXY_CLASS, serverSide = Reference.COMMON_PROXY_CLASS)
	public static CommonProxy proxy;
	
	@EventHandler
	public static void preInit(FMLInitializationEvent event)
	{
		if (Configs.dimension.generateDim)
			DimensionManager.registerDimension(Configs.dimension.dimID, HEX_DIM);
	}
	
	@EventHandler
	public static void init(FMLInitializationEvent event)
	{
		if (Configs.dimension.forceHexGen)
		{
			DimensionManager.unregisterDimension(OVERWORLD_ID);
			DimensionManager.registerDimension(OVERWORLD_ID, DimensionType.register("overworld", "_hex", OVERWORLD_ID, WorldProviderHex.class, true));
		}
		
		worldTypeFluke = new WorldTypeFluke();
		//aTest = new WorldTypeATest();
	}
	
	@EventHandler
	public static void PostInit(FMLPostInitializationEvent event)
	{
		//BiomeEditor.removeBlacklistBiomes(); bleh TODO or something
	}
	
	@EventHandler
	public void startServer(FMLServerStartingEvent event) 
	{
		event.registerServerCommand(new DebugBiome());
		event.registerServerCommand(new SomeOtherShittyDebugCommand());
	}
	
}

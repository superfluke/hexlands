package fluke.hexlands.world;

import javax.annotation.Nullable;

import fluke.hexlands.config.Configs;
import fluke.hexlands.util.hex.Hex;
import fluke.hexlands.util.hex.Layout;
import fluke.hexlands.util.hex.Point;
import biomesoplenty.common.world.BOPWorldSettings;
import biomesoplenty.common.world.BiomeProviderBOPHell;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.init.Biomes;
import net.minecraft.util.ReportedException;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeCache;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;

public class HexHellBoPBiomeProvider extends BiomeProvider
{
	
	protected Layout hex_layout = new Layout(Layout.flat, new Point(Configs.worldgen.hexSize, Configs.worldgen.hexSize), new Point(0, 0));
    private GenLayer genBiomes;
    private GenLayer biomeIndexLayer;
    private final BiomeCache biomeCache;

	
	public HexHellBoPBiomeProvider(long seed, WorldType worldType)
    {
        super();
        this.biomeCache = new BiomeCache(this);
        GenLayer[] genlayers = BiomeProviderBOPHell.setupBOPGenLayers(seed, new BOPWorldSettings(""));
        this.genBiomes = genlayers[0];
        this.biomeIndexLayer = genlayers[1];
    }
	
	public HexHellBoPBiomeProvider(World world)
    {
		this(world.getSeed(), world.getWorldInfo().getTerrainType());
    }
	
	@Override
	public Biome[] getBiomesForGeneration(Biome[] biomes, int chunkX, int chunkZ, int width, int height)
    {
        IntCache.resetIntCache();

        if (biomes == null || biomes.length < width * height)
        {
            biomes = new Biome[width * height];
        }

        //int[] aint = this.genBiomes.getInts(x, z, width, height);

        try
        {
        	Hex prev_hex = hex_layout.pixelToHex(new Point(chunkX-1000, chunkZ-1000)).hexRound();
        	Biome prev_biome = Biomes.DEFAULT;
            for (int i = 0; i < width * height; ++i)
            {
            	int realX = (i%width)+chunkX;
            	int realZ = (int)(i/height)+chunkZ;
            	Hex hexy = hex_layout.pixelToHex(new Point(realX, realZ)).hexRound();
            	
            	if (hexy.q != prev_hex.q || hexy.r != prev_hex.r)
            	{
            		prev_biome = Biome.getBiome(this.biomeIndexLayer.getInts(hexy.q*Configs.worldgen.biomeSize, hexy.r*Configs.worldgen.biomeSize, 1, 1)[0], Biomes.HELL);
            		prev_hex = hexy;
            	}
            	
            	biomes[i] = prev_biome;
            }

            return biomes;
        }
        catch (Throwable throwable)
        {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Invalid Biome id");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("RawBiomeBlock");
            crashreportcategory.addCrashSection("biomes[] size", Integer.valueOf(biomes.length));
            crashreportcategory.addCrashSection("x", Integer.valueOf(chunkX));
            crashreportcategory.addCrashSection("z", Integer.valueOf(chunkZ));
            crashreportcategory.addCrashSection("w", Integer.valueOf(width));
            crashreportcategory.addCrashSection("h", Integer.valueOf(height));
            throw new ReportedException(crashreport);
        }
    }
	
	@Override
	 public Biome[] getBiomes(@Nullable Biome[] oldBiomeList, int x, int z, int width, int depth)
    {
        return this.getBiomes(oldBiomeList, x, z, width, depth, true);
    }
	
	@Override
	public Biome[] getBiomes(@Nullable Biome[] listToReuse, int chunkX, int chunkZ, int width, int length, boolean cacheFlag)
    {
        IntCache.resetIntCache();

        if (listToReuse == null || listToReuse.length < width * length)
        {
            listToReuse = new Biome[width * length];
        }

        if (cacheFlag && width == 16 && length == 16 && (chunkX & 15) == 0 && (chunkZ & 15) == 0)
        {
            Biome[] abiome = this.biomeCache.getCachedBiomes(chunkX, chunkZ);
            System.arraycopy(abiome, 0, listToReuse, 0, width * length);
            return listToReuse;
        }
        else 
        {
            //int[] aint = this.biomeIndexLayer.getInts(x, z, width, length);
        	
        	Hex prev_hex = hex_layout.pixelToHex(new Point(chunkX-1000, chunkZ-1000)).hexRound();
        	Biome prev_biome = Biomes.DEFAULT;
            for (int i = 0; i < width * length; ++i)
            {
            	//get the hex cords of the current point
            	int realX = (i%width)+chunkX;
            	int realZ = (int)(i/length)+chunkZ;
            	Hex hexy = hex_layout.pixelToHex(new Point(realX, realZ)).hexRound();
            	
            	//if this hex has different cords from the last hex
            	if (hexy.q != prev_hex.q || hexy.r != prev_hex.r)
            	{	//get a new biome based on current hex cords
            		prev_biome = Biome.getBiome(this.biomeIndexLayer.getInts(hexy.q*Configs.worldgen.biomeSize, hexy.r*Configs.worldgen.biomeSize, 1, 1)[0], Biomes.HELL);
            		prev_hex = hexy;
            	}
            	
                listToReuse[i] = prev_biome;
            }

            return listToReuse;
        }
    }
	

}

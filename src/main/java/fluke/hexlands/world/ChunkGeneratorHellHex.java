package fluke.hexlands.world;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import fluke.hexlands.config.Configs;
import fluke.hexlands.util.SimplexNoise;
import fluke.hexlands.util.hex.Hex;
import fluke.hexlands.util.hex.Layout;
import fluke.hexlands.util.hex.Point;
import fluke.hexlands.util.hex.TestEdge;

import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockMatcher;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.MapGenCavesHell;
import net.minecraft.world.gen.NoiseGeneratorOctaves;
import net.minecraft.world.gen.feature.WorldGenBush;
import net.minecraft.world.gen.feature.WorldGenFire;
import net.minecraft.world.gen.feature.WorldGenGlowStone1;
import net.minecraft.world.gen.feature.WorldGenGlowStone2;
import net.minecraft.world.gen.feature.WorldGenHellLava;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraft.world.gen.structure.MapGenNetherBridge;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class ChunkGeneratorHellHex implements IChunkGenerator
{
    protected static final IBlockState AIR = Blocks.AIR.getDefaultState();
    protected static final IBlockState NETHERRACK = Blocks.NETHERRACK.getDefaultState();
    protected static final IBlockState BEDROCK = Blocks.BEDROCK.getDefaultState();
    protected static final IBlockState LAVA = Blocks.LAVA.getDefaultState();
    protected static final IBlockState GRAVEL = Blocks.GRAVEL.getDefaultState();
    protected static final IBlockState SOUL_SAND = Blocks.SOUL_SAND.getDefaultState();
    protected static final IBlockState NETHER_WART_BLOCK = Blocks.NETHER_WART_BLOCK.getDefaultState();
    private final World world;
    private final boolean generateStructures;
    private final Random rand;
//    /** Holds the noise used to determine whether slowsand can be generated at a location */
//    private double[] slowsandNoise = new double[256];
//    private double[] gravelNoise = new double[256];
    private final WorldGenFire fireFeature = new WorldGenFire();
    private final WorldGenGlowStone1 lightGemGen = new WorldGenGlowStone1();
    private final WorldGenGlowStone2 hellPortalGen = new WorldGenGlowStone2();
    private final WorldGenerator quartzGen = new WorldGenMinable(Blocks.QUARTZ_ORE.getDefaultState(), 14, BlockMatcher.forBlock(Blocks.NETHERRACK));
    private final WorldGenerator magmaGen = new WorldGenMinable(Blocks.MAGMA.getDefaultState(), 33, BlockMatcher.forBlock(Blocks.NETHERRACK));
    private final WorldGenHellLava lavaTrapGen = new WorldGenHellLava(Blocks.FLOWING_LAVA, true);
    private final WorldGenHellLava hellSpringGen = new WorldGenHellLava(Blocks.FLOWING_LAVA, false);
    private final WorldGenBush brownMushroomFeature = new WorldGenBush(Blocks.BROWN_MUSHROOM);
    private final WorldGenBush redMushroomFeature = new WorldGenBush(Blocks.RED_MUSHROOM);
    private MapGenNetherBridge genNetherBridge = new MapGenNetherBridge();
    private MapGenBase genNetherCaves = new MapGenCavesHell();

    protected Layout hex_layout = new Layout(Layout.flat, new Point(Configs.worldgen.hexSize, Configs.worldgen.hexSize), new Point(0, 0));
    public static int netherSeaLevel = 31;
    public static int netherMaxHeight = 128;
    private IBlockState rimBlock;

    public ChunkGeneratorHellHex(World worldIn, boolean generateStructures, long seed)
    {
    	updateRimBlock();
        this.world = worldIn;
        this.generateStructures = generateStructures;
        this.rand = new Random(seed);
//        this.lperlinNoise1 = new NoiseGeneratorOctaves(this.rand, 16);
//        this.lperlinNoise2 = new NoiseGeneratorOctaves(this.rand, 16);
//        this.perlinNoise1 = new NoiseGeneratorOctaves(this.rand, 8);
//        this.slowsandGravelNoiseGen = new NoiseGeneratorOctaves(this.rand, 4);
//        this.netherrackExculsivityNoiseGen = new NoiseGeneratorOctaves(this.rand, 4);
//        this.scaleNoise = new NoiseGeneratorOctaves(this.rand, 10);
//        this.depthNoise = new NoiseGeneratorOctaves(this.rand, 16);
//        worldIn.setSeaLevel(63);
//
//        net.minecraftforge.event.terraingen.InitNoiseGensEvent.ContextHell ctx =
//                new net.minecraftforge.event.terraingen.InitNoiseGensEvent.ContextHell(lperlinNoise1, lperlinNoise2, perlinNoise1, slowsandGravelNoiseGen, netherrackExculsivityNoiseGen, scaleNoise, depthNoise);
//        ctx = net.minecraftforge.event.terraingen.TerrainGen.getModdedNoiseGenerators(worldIn, this.rand, ctx);
//        this.lperlinNoise1 = ctx.getLPerlin1();
//        this.lperlinNoise2 = ctx.getLPerlin2();
//        this.perlinNoise1 = ctx.getPerlin();
//        this.slowsandGravelNoiseGen = ctx.getPerlin2();
//        this.netherrackExculsivityNoiseGen = ctx.getPerlin3();
//        this.scaleNoise = ctx.getScale();
//        this.depthNoise = ctx.getDepth();
        this.genNetherBridge = (MapGenNetherBridge)net.minecraftforge.event.terraingen.TerrainGen.getModdedMapGen(genNetherBridge, net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.NETHER_BRIDGE);
        this.genNetherCaves = net.minecraftforge.event.terraingen.TerrainGen.getModdedMapGen(genNetherCaves, net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.NETHER_CAVE);
    }


    public void generateTerrain(final int chunkX, final int chunkZ, final ChunkPrimer primer)
    {
    	
    	
    	for (int x = 0; x < 16; x++) //TODO round noise stuff to avoid crop circles again
        {
            final int realX = x + chunkX * 16;
            
            for (int z = 0; z < 16; z++)
            {
                final int realZ = z + chunkZ * 16;
                int hexHeight = 35;
                int hexRoofDepth = 20;
                
                //convert x,z to a hex cords (q,r)
                Hex hexy = hex_layout.pixelToHex(new Point(realX, realZ)).hexRound();
                
                //convert hex cords back to x,z to get center point
                Point center_pt =  hex_layout.hexToPixel(hexy);
                
                //some hexes will just be solid from top to bottom
                double wallNoise = SimplexNoise.noise(center_pt.getX()/80, center_pt.getZ()/80);
                boolean solidHex = (wallNoise > 0.4);
                
                if(solidHex)
                {
                	for(int y=1; y<netherMaxHeight; y++)
                    {
                    	 primer.setBlockState(x, y, z, NETHERRACK);
                    }
                }
                else
                {
                    double hexNoise = SimplexNoise.noise(center_pt.getX()/40, center_pt.getZ()/40);
                    double roofHeightNoise = SimplexNoise.noise(center_pt.getX()/20, center_pt.getZ()/20);
                    
                    double terrainNoise = SimplexNoise.noise(realX, realZ, 160, 160, 0.5, 2);
                    double terrainNoiser = SimplexNoise.noise(realX, realZ, 60, 60, 0.5, 4);
                    double roofNoise = SimplexNoise.noise(realX, realZ, 120, 120, 0.5, 2);
                    double roofNoiser = SimplexNoise.noise(realX, realZ, 80, 80, 0.5, 4);
                    
                    double hexTerrainNoise = terrainNoise + 0.7*terrainNoiser;
                    double roofTerrainNoise = roofNoise + 0.7*roofNoiser;
                    double midlandTerrainNoise = terrainNoise*0.8 + 0.5*roofNoise + 0.5*roofNoiser;
                    double midlandRoofNoise = Math.abs(roofNoise*0.8 + 0.5*terrainNoise + 0.5*terrainNoiser);
                    
                    boolean isEdgeBlock = TestEdge.isEdge(new Point(realX, realZ), center_pt, hexy, Configs.worldgen.hexSize, Configs.worldgen.hexSize);
                    
                    //y level of lower hex before we adjust for terrain noise 
                    hexHeight += (hexNoise*26);
                    
                    //y level terrain should extend down from top bedrock
                    hexRoofDepth += (roofHeightNoise*16);
                    int roofDepth = (int) (hexRoofDepth + (roofTerrainNoise*6));
                    
                    //y level of in between layers if we decide to use it
                    int midLand = ((netherMaxHeight - hexRoofDepth) + hexHeight)/2;
                    
                    //get distance to center point of hex
                	int xdif = realX - center_pt.getX();
                	int zdif = realZ - center_pt.getZ();
                	double distance_from_origin = Math.sqrt(xdif*xdif+zdif*zdif);
                	double distance_ratio = distance_from_origin/Configs.worldgen.hexSize;
                	if (distance_ratio > 0.9)
                		distance_ratio = 0.9;
                	
                	int blockDesiredHeight = (int)Math.round(hexHeight + (hexTerrainNoise*6));
                	
                	//smooth out where the terrain wants to be with the height of the hex rim based on distance from center of hex
                	int finalBlockHeight = (int)Math.round((blockDesiredHeight*(1-distance_ratio) + hexHeight*distance_ratio));
                    

                    for(int y=1; y<finalBlockHeight; y++)
                    {
                    	//fill in lower hex from bottom bedrock to block height
                    	 primer.setBlockState(x, y, z, NETHERRACK);
                    }
                    
                    if(finalBlockHeight <= netherSeaLevel)
                    {
                    	for(int y=finalBlockHeight; y<=netherSeaLevel; y++)
                        {
                    		//fill in lava if hex is lower than the nether 'sea level'
                        	 primer.setBlockState(x, y, z, LAVA);
                        }
                    }
                    else if(isEdgeBlock)
                    {
                    	for(int y=finalBlockHeight; y<hexHeight; y++)
                        {
                    		//fill in blocks under rim if they are missing
                    		primer.setBlockState(x, y, z, NETHERRACK);
                        }
                    	//draw the rim
                    	primer.setBlockState(x, hexHeight, z, rimBlock);
                    }
                    
                    for(int y2=(netherMaxHeight-roofDepth); y2<netherMaxHeight; y2++)
                    {
                    	//fill in top layer of netherrack for the roof
                    	primer.setBlockState(x, y2, z, NETHERRACK);
                    }
                    
                    
                    double midLandNoise = SimplexNoise.noise(center_pt.getX()/20, midLand, center_pt.getZ()/20);
                    
                    //use simplex noise to decide when to draw midlands. should result in more connected hexes than pure rng
                    if(midLandNoise > 0.1)
                    {
                    	int midThicc = (int)(Math.abs(midLandNoise)*6) + 3;
                    	
                    	int midHexBottom = (int) (midLand - midThicc);
                    	int midBottomDesiredHeight = (int)Math.round(midHexBottom - (midlandRoofNoise * 4));
                    	int midHexTop = (int) (midLand + midThicc);
                    	int midTopDesiredHeight = (int)Math.round(midHexTop + (midlandTerrainNoise  * 4));
                    	
                    	//reuse same distance ratio from before since hexes are stacked
                    	int finalBottomBlockHeight = (int)Math.round((midBottomDesiredHeight*(1-distance_ratio) + midHexBottom*distance_ratio));
                    	int finalTopBlockHeight = (int)Math.round((midTopDesiredHeight*(1-distance_ratio) + midHexTop*distance_ratio));
                    	
                    	for (int midY=finalBottomBlockHeight; midY<finalTopBlockHeight; midY++)
                    	{
                    		primer.setBlockState(x, midY, z, NETHERRACK);
                    	}
                    	
                    	if(isEdgeBlock)
                    	{
                    		primer.setBlockState(x, midHexTop, z, rimBlock);
//                    		primer.setBlockState(x, midHexBottom, z, NETHER_WART_BLOCK);
                    	}
                    }
                }
                
                //set top and bottom bedrock
                primer.setBlockState(x, 0, z, BEDROCK);
                primer.setBlockState(x, netherMaxHeight, z, BEDROCK);
            }
        }
    }

    /**
     * Generates the chunk at the specified position, from scratch
     */
    public Chunk generateChunk(int x, int z)
    {
        this.rand.setSeed((long)x * 341873128712L + (long)z * 132897987541L);
        ChunkPrimer chunkprimer = new ChunkPrimer();

        this.generateTerrain(x, z, chunkprimer);
        this.genNetherCaves.generate(this.world, x, z, chunkprimer);

        if (this.generateStructures)
        {
            this.genNetherBridge.generate(this.world, x, z, chunkprimer);
        }

        Chunk chunk = new Chunk(this.world, chunkprimer, x, z);
//        Biome[] abiome = this.world.getBiomeProvider().getBiomes((Biome[])null, x * 16, z * 16, 16, 16);
//        byte[] abyte = chunk.getBiomeArray();
//
//        for (int i = 0; i < abyte.length; ++i)
//        {
//            abyte[i] = (byte)Biome.getIdForBiome(abiome[i]);
//        }

        chunk.resetRelightChecks();
        return chunk;
    }


    /**
     * Generate initial structures in this chunk, e.g. mineshafts, temples, lakes, and dungeons
     */
    public void populate(int x, int z)
    {
        BlockFalling.fallInstantly = true;
        net.minecraftforge.event.ForgeEventFactory.onChunkPopulate(true, this, this.world, this.rand, x, z, false);
        int i = x * 16;
        int j = z * 16;
        BlockPos blockpos = new BlockPos(i, 0, j);
        Biome biome = this.world.getBiome(blockpos.add(16, 0, 16));
        ChunkPos chunkpos = new ChunkPos(x, z);
        this.genNetherBridge.generateStructure(this.world, this.rand, chunkpos);

        if (net.minecraftforge.event.terraingen.TerrainGen.populate(this, this.world, this.rand, x, z, false, net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.NETHER_LAVA))
        for (int k = 0; k < 8; ++k)
        {
            this.hellSpringGen.generate(this.world, this.rand, blockpos.add(this.rand.nextInt(16) + 8, this.rand.nextInt(120) + 4, this.rand.nextInt(16) + 8));
        }

        if (net.minecraftforge.event.terraingen.TerrainGen.populate(this, this.world, this.rand, x, z, false, net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.FIRE))
        for (int i1 = 0; i1 < this.rand.nextInt(this.rand.nextInt(10) + 1) + 1; ++i1)
        {
            this.fireFeature.generate(this.world, this.rand, blockpos.add(this.rand.nextInt(16) + 8, this.rand.nextInt(120) + 4, this.rand.nextInt(16) + 8));
        }

        if (net.minecraftforge.event.terraingen.TerrainGen.populate(this, this.world, this.rand, x, z, false, net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.GLOWSTONE))
        {
        for (int j1 = 0; j1 < this.rand.nextInt(this.rand.nextInt(10) + 1); ++j1)
        {
            this.lightGemGen.generate(this.world, this.rand, blockpos.add(this.rand.nextInt(16) + 8, this.rand.nextInt(120) + 4, this.rand.nextInt(16) + 8));
        }

        for (int k1 = 0; k1 < 10; ++k1)
        {
            this.hellPortalGen.generate(this.world, this.rand, blockpos.add(this.rand.nextInt(16) + 8, this.rand.nextInt(128), this.rand.nextInt(16) + 8));
        }
        }//Forge: End doGLowstone

        net.minecraftforge.event.ForgeEventFactory.onChunkPopulate(false, this, this.world, this.rand, x, z, false);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.terraingen.DecorateBiomeEvent.Pre(this.world, this.rand, blockpos));

        if (net.minecraftforge.event.terraingen.TerrainGen.decorate(this.world, this.rand, blockpos, net.minecraftforge.event.terraingen.DecorateBiomeEvent.Decorate.EventType.SHROOM))
        {
        if (this.rand.nextBoolean())
        {
            this.brownMushroomFeature.generate(this.world, this.rand, blockpos.add(this.rand.nextInt(16) + 8, this.rand.nextInt(128), this.rand.nextInt(16) + 8));
        }

        if (this.rand.nextBoolean())
        {
            this.redMushroomFeature.generate(this.world, this.rand, blockpos.add(this.rand.nextInt(16) + 8, this.rand.nextInt(128), this.rand.nextInt(16) + 8));
        }
        }


        if (net.minecraftforge.event.terraingen.TerrainGen.generateOre(this.world, this.rand, quartzGen, blockpos, net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable.EventType.QUARTZ))
        for (int l1 = 0; l1 < 16; ++l1)
        {
            this.quartzGen.generate(this.world, this.rand, blockpos.add(this.rand.nextInt(16), this.rand.nextInt(108) + 10, this.rand.nextInt(16)));
        }

        int i2 = this.world.getSeaLevel() / 2 + 1;

        if (net.minecraftforge.event.terraingen.TerrainGen.populate(this, this.world, this.rand, x, z, false, net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.NETHER_MAGMA))
        for (int l = 0; l < 4; ++l)
        {
            this.magmaGen.generate(this.world, this.rand, blockpos.add(this.rand.nextInt(16), i2 - 5 + this.rand.nextInt(10), this.rand.nextInt(16)));
        }

        if (net.minecraftforge.event.terraingen.TerrainGen.populate(this, this.world, this.rand, x, z, false, net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.NETHER_LAVA2))
        for (int j2 = 0; j2 < 16; ++j2)
        {
            int offset = net.minecraftforge.common.ForgeModContainer.fixVanillaCascading ? 8 : 0; // MC-117810
            this.lavaTrapGen.generate(this.world, this.rand, blockpos.add(this.rand.nextInt(16) + offset, this.rand.nextInt(108) + 10, this.rand.nextInt(16) + offset));
        }

        biome.decorate(this.world, this.rand, new BlockPos(i, 0, j));

        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.terraingen.DecorateBiomeEvent.Post(this.world, this.rand, blockpos));

        BlockFalling.fallInstantly = false;
    }

    /**
     * Called to generate additional structures after initial worldgen, used by ocean monuments
     */
    public boolean generateStructures(Chunk chunkIn, int x, int z)
    {
        return false;
    }

    public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos)
    {
        if (creatureType == EnumCreatureType.MONSTER)
        {
            if (this.genNetherBridge.isInsideStructure(pos))
            {
                return this.genNetherBridge.getSpawnList();
            }

            if (this.genNetherBridge.isPositionInStructure(this.world, pos) && this.world.getBlockState(pos.down()).getBlock() == Blocks.NETHER_BRICK)
            {
                return this.genNetherBridge.getSpawnList();
            }
        }

        Biome biome = this.world.getBiome(pos);
        return biome.getSpawnableList(creatureType);
    }

    @Nullable
    public BlockPos getNearestStructurePos(World worldIn, String structureName, BlockPos position, boolean findUnexplored)
    {
        return "Fortress".equals(structureName) && this.genNetherBridge != null ? this.genNetherBridge.getNearestStructurePos(worldIn, position, findUnexplored) : null;
    }

    public boolean isInsideStructure(World worldIn, String structureName, BlockPos pos)
    {
        return "Fortress".equals(structureName) && this.genNetherBridge != null ? this.genNetherBridge.isInsideStructure(pos) : false;
    }

    /**
     * Recreates data about structures intersecting given chunk (used for example by getPossibleCreatures), without
     * placing any blocks. When called for the first time before any chunk is generated - also initializes the internal
     * state needed by getPossibleCreatures.
     */
    public void recreateStructures(Chunk chunkIn, int x, int z)
    {
        this.genNetherBridge.generate(this.world, x, z, (ChunkPrimer)null);
    }
    
    public void updateRimBlock() {
    	String rimblock = Configs.nether.netherRimBlock;
    	if(rimblock.indexOf("@") > 0)
    	{
    		String[] metarim = rimblock.split("@");
    		rimBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(metarim[0])).getStateFromMeta(Integer.parseInt(metarim[1]));
    	}
    	else
    	{
    		rimBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(rimblock)).getDefaultState();
    	}
    }
}

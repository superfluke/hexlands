package fluke.hexlands.world;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import fluke.hexlands.config.Configs;
import fluke.hexlands.util.SimplexNoise;
import fluke.hexlands.util.hex.Hex;
import fluke.hexlands.util.hex.Layout;
import fluke.hexlands.util.hex.Point;
import fluke.hexlands.util.hex.TestEdge;

//import com.bloodnbonesgaming.dimensionalcontrol.util.noise.OpenSimplexNoiseGeneratorOctaves;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEntitySpawner;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.MapGenCaves;
import net.minecraft.world.gen.MapGenRavine;
import net.minecraft.world.gen.feature.WorldGenDungeons;
import net.minecraft.world.gen.feature.WorldGenLakes;
import net.minecraft.world.gen.structure.MapGenMineshaft;
import net.minecraft.world.gen.structure.MapGenScatteredFeature;
import net.minecraft.world.gen.structure.MapGenStronghold;
import net.minecraft.world.gen.structure.MapGenVillage;
import net.minecraft.world.gen.structure.StructureOceanMonument;
import net.minecraft.world.gen.structure.WoodlandMansion;

public class ChunkGeneratorOverworldHex implements IChunkGenerator
{
    final Random rand = new Random();
    final World world;
    Biome[] biomesForGeneration;
    protected static final IBlockState WATER = Blocks.WATER.getDefaultState();
    protected static final IBlockState STONE = Blocks.STONE.getDefaultState();
    //protected final OpenSimplexNoiseGeneratorOctaves terrainNoise;
    //protected final SimplexNoise simnoise;
    public static final int HEX_X_SIZE = Configs.hexWidth;
    public static final int HEX_Z_SIZE = Configs.hexHeight;
    public static final int SEA_LEVEL = 60;
    protected Layout hex_layout = new Layout(Layout.flat, new Point(HEX_X_SIZE, HEX_Z_SIZE), new Point(0, 0));

    private MapGenBase caveGenerator = new MapGenCaves();
    private MapGenStronghold strongholdGenerator = new MapGenStronghold();
    private MapGenVillage villageGenerator = new MapGenVillage();
    private MapGenMineshaft mineshaftGenerator = new MapGenMineshaft();
    private MapGenScatteredFeature scatteredFeatureGenerator = new MapGenScatteredFeature();
    private MapGenBase ravineGenerator = new MapGenRavine();
    private StructureOceanMonument oceanMonumentGenerator = new StructureOceanMonument();
    public IBlockState rim = Block.getBlockFromName(Configs.rimBlock).getDefaultState();
    //private WoodlandMansion woodlandMansionGenerator = new WoodlandMansion(this);
    public IBlockState rim2 = Block.getBlockFromName("minecraft:netherrack").getDefaultState();
    
    public ChunkGeneratorOverworldHex(final World world)
    {
    	{
            caveGenerator = net.minecraftforge.event.terraingen.TerrainGen.getModdedMapGen(caveGenerator, net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.CAVE);
            strongholdGenerator = (MapGenStronghold)net.minecraftforge.event.terraingen.TerrainGen.getModdedMapGen(strongholdGenerator, net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.STRONGHOLD);
            villageGenerator = (MapGenVillage)net.minecraftforge.event.terraingen.TerrainGen.getModdedMapGen(villageGenerator, net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.VILLAGE);
            mineshaftGenerator = (MapGenMineshaft)net.minecraftforge.event.terraingen.TerrainGen.getModdedMapGen(mineshaftGenerator, net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.MINESHAFT);
            scatteredFeatureGenerator = (MapGenScatteredFeature)net.minecraftforge.event.terraingen.TerrainGen.getModdedMapGen(scatteredFeatureGenerator, net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.SCATTERED_FEATURE);
            ravineGenerator = net.minecraftforge.event.terraingen.TerrainGen.getModdedMapGen(ravineGenerator, net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.RAVINE);
            oceanMonumentGenerator = (StructureOceanMonument)net.minecraftforge.event.terraingen.TerrainGen.getModdedMapGen(oceanMonumentGenerator, net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.OCEAN_MONUMENT);
            //woodlandMansionGenerator = (WoodlandMansion)net.minecraftforge.event.terraingen.TerrainGen.getModdedMapGen(woodlandMansionGenerator, net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.WOODLAND_MANSION);
        }
        this.world = world;
        //terrainNoise = new OpenSimplexNoiseGeneratorOctaves(world.getSeed());
        //simnoise = new SimplexNoise();
    }

    @Override
    public Chunk generateChunk(int x, int z)
    {
    	
        this.rand.setSeed((long)x * 341873128712L + (long)z * 132897987541L);
        ChunkPrimer chunkprimer = new ChunkPrimer();
        
        this.biomesForGeneration = this.world.getBiomeProvider().getBiomes(this.biomesForGeneration, x * 16, z * 16, 16, 16);
        this.generateTerrain(x, z, chunkprimer);
        this.replaceBiomeBlocks(x, z, chunkprimer, this.biomesForGeneration);
        
        if(Configs.generateCaves)
        {
	        this.caveGenerator.generate(this.world, x, z, chunkprimer);
	        this.ravineGenerator.generate(this.world, x, z, chunkprimer);
        }

        Chunk chunk = new Chunk(this.world, chunkprimer, x, z);
        byte[] abyte = chunk.getBiomeArray();

        for (int i = 0; i < abyte.length; ++i)
        {
            abyte[i] = (byte)Biome.getIdForBiome(this.biomesForGeneration[i]);
        }

        chunk.generateSkylightMap();
        return chunk;
    }
    
    public void replaceBiomeBlocks(int x, int z, ChunkPrimer primer, Biome[] biomesIn)
    {
        if (!net.minecraftforge.event.ForgeEventFactory.onReplaceBiomeBlocks(this, x, z, primer, this.world)) return;
        double d0 = 0.03125D;
        //this.depthBuffer = this.surfaceNoise.getRegion(this.depthBuffer, (double)(x * 16), (double)(z * 16), 16, 16, 0.0625D, 0.0625D, 1.0D);

        for (int i = 0; i < 16; ++i)
        {
            for (int j = 0; j < 16; ++j)
            {
                Biome biome = biomesIn[j + i * 16];
                biome.genTerrainBlocks(this.world, this.rand, primer, x * 16 + i, z * 16 + j, 0.5);
            }
        }
    }
    
    public void generateTerrain(final int chunkX, final int chunkZ, final ChunkPrimer primer)
    {
        
        //System.out.printf("chunk x, z: %d, %d = %d, %d\n", chunkX, chunkZ, chunkX*16, chunkZ*16);
        for (int x = 0; x < 16; x++)
        {
            final int realX = x + chunkX * 16;
            
            for (int z = 0; z < 16; z++)
            {
                final int realZ = z + chunkZ * 16;
                //convert x,z to a hex
                Hex hexy = hex_layout.pixelToHex(new Point(realX, realZ)).hexRound();
                
                //convert hex cords back to x,z to get center point
                Point center_pt =  hex_layout.hexToPixel(hexy);

                boolean isEdgeBlock = TestEdge.isEdge(new Point(realX, realZ), center_pt, hexy, HEX_X_SIZE, HEX_Z_SIZE);
                boolean isHardEdge = false;
                boolean isBeachEdge = false;
                
                Biome this_biome = this.world.getBiomeProvider().getBiome(new BlockPos(realX, 90, realZ));
                boolean isWet = this_biome == Biomes.OCEAN || this_biome == Biomes.DEEP_OCEAN;
                boolean isBeach = this_biome == Biomes.BEACH || this_biome == Biomes.STONE_BEACH;
                
                
                //get noise at hex cords
                //double hex_noise = SimplexNoise.noise(hexy.q, hexy.r);
                double hex_noise = SimplexNoise.noise(center_pt.getX()/60, center_pt.getZ()/60);
                
                float biomeBaseHeight = this_biome.getBaseHeight();
                float biomeVariation = this_biome.getHeightVariation();
                float bVar = biomeVariation * 0.6F + 0.1F;
                float bBas = (biomeBaseHeight * 16.0F - 2.0F) / 8.0F;
                
                double noise = SimplexNoise.noise(realX, realZ, 100, 100, 0.5, 6);
                double noiser = SimplexNoise.noise(realX, realZ, 40, 40, 0.5, 2);
                double noisyist = SimplexNoise.noise(realX, realZ, 20, 20, 0.5, 2);
                noise += noiser*0.9 + noisyist * 0.6;
                noise *= Math.abs(noise+0.5)*0.8;
                
                //figure out if we need to draw a line between 2 hexes
                if(!isWet && !Configs.outlineAll)
            	{
	                //int direction = -1;
	                if (isEdgeBlock)
	                {	                	
	                	//this is sumdum shit, if anyone sees this... i'm sorry
	                	//figure out what direction to check for biome matching
	                	//south east edge = 0 and increases going counter-clockwise around the hex
	                	
	                	ArrayList<Integer> directions_to_test = new ArrayList<Integer>();
	                	
	                	if (realX - center_pt.getX() > 0)
	                	{//east side
	                		int pretestZ = realZ - center_pt.getZ();
	                		int testZ = realZ - center_pt.getZ();
	                		int testX = realX - center_pt.getX();
	                		int boundry_size = 3; 
	                		boolean zPositive = true;
	                		if (testZ <= 0)
	                		{
	                			testZ = Math.abs(testZ);
	                			zPositive = false;
	                		}

                			double inner_diagonal = Hex.sqr3 * ((HEX_X_SIZE-(boundry_size+1))-testX); //CHANGE TO ACTUAL BOUNDRY SIZE 
                			double inner_flat = Hex.sqr3 * ((HEX_X_SIZE-boundry_size)/2);
                			
                			if (testZ > inner_diagonal)
                			{//the slanty sides on the east part of the hex
                				
                				if (testZ <= 2) 
                				{//add 2 checks for the 3 'center' edge blocks on slanty sides, again for cleaner corners
                					if ((pretestZ*-1) > (inner_diagonal+testZ))
                					{
                						if (zPositive)
                        					directions_to_test.add(1);
                        				else
                        					directions_to_test.add(0);
                					}
                				}
                				
                				if (zPositive)
                					directions_to_test.add(0);
                				else
                					directions_to_test.add(1);
                				
                				if(testZ > inner_flat)
                				{//this catches the points where the slant side and the flat side overlap to generate cleaner corners
                					if (zPositive)
                    					directions_to_test.add(5);
                    				else
                    					directions_to_test.add(2);
                				}
                				
                			}
                			else
                			{//not slanty, so must be the top or bottom of the hex
                				if (zPositive)
                					directions_to_test.add(5);
                				else
                					directions_to_test.add(2);
                			}
	                		
	                	}
	                	else
	                	{//west side
	                		int pretestZ = realZ - center_pt.getZ();
	                		int testZ = pretestZ;
	                		int testX = Math.abs(realX - center_pt.getX());
	                		int boundry_size = 3;
	                		boolean zPositive = true;
	                		if (testZ <= 0)
	                		{
	                			testZ = Math.abs(testZ);
	                			zPositive = false;
	                		}

                			double inner_diagonal = Hex.sqr3 * ((HEX_X_SIZE-(boundry_size+1))-testX); //CHANGE TO ACTUAL BOUNDRY SIZE 
                			double inner_flat = Hex.sqr3 * ((HEX_X_SIZE-boundry_size)/2);
                			
                			if (testZ > inner_diagonal)
                			{//the slanty sides on the west part of the hex
                				
                				if (testZ <= 2) 
                				{//add 2 checks for the 3 'center' edge blocks on slanty sides, again for cleaner corners
                					if ((pretestZ*-1) > (inner_diagonal+testZ))
                					{
                						if (zPositive)
                        					directions_to_test.add(3);
                        				else
                        					directions_to_test.add(4);
                					}
                				}
                				
                				if (zPositive)
                					directions_to_test.add(4);
                				else
                					directions_to_test.add(3);
                				
                				if(testZ > inner_flat)
                				{//this catches the points where the slant side and the flat side overlap to generate cleaner corners
                					if (zPositive)
                    					directions_to_test.add(5);
                    				else
                    					directions_to_test.add(2);
                				}
                			}
                			else
                			{//not slanty, so must be the top or bottom of the hex
                				if (zPositive)
                					directions_to_test.add(5);
                				else
                					directions_to_test.add(2);
                			}
	                	}
	                	
	                	for (Integer direction : directions_to_test) { 		      
		
	                   
		                	//check the biome next to current hex
		                	Hex hex_next_door = hexy.neighbor(direction);
		                	Point neighbor_origin =  hex_layout.hexToPixel(hex_next_door);
		                	Biome neighbor_biome = this.world.getBiomeProvider().getBiome(new BlockPos(neighbor_origin.getX(), 90, neighbor_origin.getZ()));
		                	
		                	//if biomes don't match build a wall
		                	if(this_biome != neighbor_biome)
		                	{
		                		isHardEdge = true;
		                		
		                		/*
		                		if(isBeach)
		                		{
		                			boolean neighborIsWet = neighbor_biome == Biomes.DEEP_OCEAN || neighbor_biome == Biomes.OCEAN;
		                			if (neighborIsWet)
		                			{
		                				isHardEdge = false;
		                				isBeachEdge = true;
		                			}
		                		}
		                		*/
		                		
		                	}

		                	
		                	/*
		                	for (int y = 42; y <= height; y++)
		                    {
		                    	primer.setBlockState(x, y, z, edge);
		                    }
		                    */
	                	}
	
	                }
            	}
                //int height = (int) (60 + 2 * bBas + (50 * noise)*(bVar));
                //adjust height by noise
                //int height = (int)(60 + 14*(hex_noise+this_biome.getBaseHeight()));
                int hex_height = (int)(Configs.terrainBaseline + 3 * bBas);// + 3*hex_noise);
                int block_height = hex_height;
                
                if(isWet)
                {
                	hex_height -= 26;
                	block_height = hex_height;
                	//block_height += (int)((50 * noise)*(bVar));
                }
                /*
                else if(isBeach)
                {
                	hex_height = (int)((hex_height + SEA_LEVEL)/2);
                	block_height = hex_height;
                }
                */
                
                if(!isHardEdge)
                {
                	//get distance to center point of hex, though this whole thing assumes equal width and height
                	int xdif = realX - center_pt.getX();
                	int zdif = realZ - center_pt.getZ();
                	double distance_from_origin = Math.sqrt(xdif*xdif+zdif*zdif);
                	double distance_ratio = distance_from_origin/HEX_X_SIZE;
                	if (distance_ratio > 0.85)
                		distance_ratio = 0.85;
                	//int block_desired_height = (int)(hex_height + 5*block_noise + 32*this_biome.getHeightVariation()*block_noiser);
                	int block_desired_height = (int) (block_height + (58 * noise)*(bVar));
                	
                	//smooth out where the terrain wants to be with the height of the hex rim based on distance from center of hex
                	block_height = (int)(block_desired_height*(1-distance_ratio) + hex_height*distance_ratio);
                	//System.out.printf("%d %d\n", block_desired_height, block_height);
                	
                	if(isBeachEdge)
                	{
                		block_height = (int)((block_height + SEA_LEVEL) / 2)-1;
                	}
                }

                if(block_height>255)
                	block_height = 255;
                else if(block_height<1)
                	block_height = 1;
                
                //set everything under height to stone
                for (int y = 0; y < block_height; y++)
                {
                	primer.setBlockState(x, y, z, STONE);
                }
                
                if (block_height < 60)
                {
                	for (int y = block_height; y < SEA_LEVEL; y++)
                		primer.setBlockState(x, y, z, WATER);
                	block_height = 60;
                }
                else
                {
                	primer.setBlockState(x, block_height, z, STONE);
                }
                

               
                /*
                int dist_to_center = (int)Math.sqrt(Math.pow(realX - center_pt.getX(), 2) + Math.pow(realZ - center_pt.getZ(), 2));
                System.out.printf("x, z: %d, %d, center x, z: %d, %d, distance: %d\n", realX, realZ, center_pt.getX(), center_pt.getZ(), dist_to_center);
                if (dist_to_center == 17)
                {
                	primer.setBlockState(x, height+1, z, Blocks.STONE.getDefaultState());
                }
                */
                
            	
            	if(realX == center_pt.getX() && realZ == center_pt.getZ()) //delete me
            		primer.setBlockState(x, block_height+1, z, rim2);
            	
            	if(Configs.outlineAll || isHardEdge)
            	//if(isHardEdge)
            	{
            		if (isEdgeBlock)
            			primer.setBlockState(x, block_height+1, z, rim);
            	}
            	
             
            }
        }
    }

    @Override
    public void populate(int x, int z)
    {
        BlockFalling.fallInstantly = true;
        int i = x * 16;
        int j = z * 16;
        BlockPos blockpos = new BlockPos(i, 0, j);
        Biome biome = this.world.getBiome(blockpos.add(16, 0, 16));
        this.rand.setSeed(this.world.getSeed());
        long k = this.rand.nextLong() / 2L * 2L + 1L;
        long l = this.rand.nextLong() / 2L * 2L + 1L;
        this.rand.setSeed((long)x * k + (long)z * l ^ this.world.getSeed());
        boolean villageHere = false;
        ChunkPos chunkpos = new ChunkPos(x, z);

        net.minecraftforge.event.ForgeEventFactory.onChunkPopulate(true, this, this.world, this.rand, x, z, villageHere);
        
        
        if (Configs.generateStructures)
        {
        	this.mineshaftGenerator.generateStructure(this.world, this.rand, chunkpos);
        	villageHere = this.villageGenerator.generateStructure(this.world, this.rand, chunkpos);
        	this.strongholdGenerator.generateStructure(this.world, this.rand, chunkpos);
        	this.scatteredFeatureGenerator.generateStructure(this.world, this.rand, chunkpos);
        	this.oceanMonumentGenerator.generateStructure(this.world, this.rand, chunkpos);
        	//this.woodlandMansionGenerator.generateStructure(this.world, this.rand, chunkpos);
        }

        if (biome != Biomes.DESERT && biome != Biomes.DESERT_HILLS && !villageHere && this.rand.nextInt(Configs.lakeRarity) == 0)
        {
	        if (net.minecraftforge.event.terraingen.TerrainGen.populate(this, this.world, this.rand, x, z, villageHere, net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.LAKE))
	        {
	            int i1 = this.rand.nextInt(16) + 8;
	            int j1 = this.rand.nextInt(256);
	            int k1 = this.rand.nextInt(16) + 8;
	            (new WorldGenLakes(Blocks.WATER)).generate(this.world, this.rand, blockpos.add(i1, j1, k1));
	        }
        }

        if (!villageHere)
        {
	        if (net.minecraftforge.event.terraingen.TerrainGen.populate(this, this.world, this.rand, x, z, villageHere, net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.LAVA))
	        {
	            int i2 = this.rand.nextInt(16) + 8;
	            int l2 = this.rand.nextInt(this.rand.nextInt(248) + 8);
	            int k3 = this.rand.nextInt(16) + 8;
	
	            if (l2 < this.world.getSeaLevel())
	            {
	                (new WorldGenLakes(Blocks.LAVA)).generate(this.world, this.rand, blockpos.add(i2, l2, k3));
	            }
	        }
        }

        
        if (net.minecraftforge.event.terraingen.TerrainGen.populate(this, this.world, this.rand, x, z, villageHere, net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.DUNGEON))
        {
            for (int j2 = 0; j2 < 8; ++j2)
            {
                int i3 = this.rand.nextInt(16) + 8;
                int l3 = this.rand.nextInt(256);
                int l1 = this.rand.nextInt(16) + 8;
                (new WorldGenDungeons()).generate(this.world, this.rand, blockpos.add(i3, l3, l1));
            }
        }

        biome.decorate(this.world, this.rand, new BlockPos(i, 0, j));
        if (net.minecraftforge.event.terraingen.TerrainGen.populate(this, this.world, this.rand, x, z, villageHere, net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.ANIMALS))
        WorldEntitySpawner.performWorldGenSpawning(this.world, biome, i + 8, j + 8, 16, 16, this.rand);
        blockpos = blockpos.add(8, 0, 8);

        if (net.minecraftforge.event.terraingen.TerrainGen.populate(this, this.world, this.rand, x, z, villageHere, net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.ICE))
        {
        for (int k2 = 0; k2 < 16; ++k2)
        {
            for (int j3 = 0; j3 < 16; ++j3)
            {
                BlockPos blockpos1 = this.world.getPrecipitationHeight(blockpos.add(k2, 0, j3));
                BlockPos blockpos2 = blockpos1.down();

                if (this.world.canBlockFreezeWater(blockpos2))
                {
                    this.world.setBlockState(blockpos2, Blocks.ICE.getDefaultState(), 2);
                }

                if (this.world.canSnowAt(blockpos1, true))
                {
                    this.world.setBlockState(blockpos1, Blocks.SNOW_LAYER.getDefaultState(), 2);
                }
            }
        }
        }//Forge: End ICE

        net.minecraftforge.event.ForgeEventFactory.onChunkPopulate(false, this, this.world, this.rand, x, z, villageHere);

        BlockFalling.fallInstantly = false;
    }

    @Override
    public boolean generateStructures(Chunk chunkIn, int x, int z)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public List<SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BlockPos getNearestStructurePos(World worldIn, String structureName, BlockPos position, boolean findUnexplored)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void recreateStructures(Chunk chunkIn, int x, int z)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean isInsideStructure(World worldIn, String structureName, BlockPos pos)
    {
        // TODO Auto-generated method stub
        return false;
    }
}

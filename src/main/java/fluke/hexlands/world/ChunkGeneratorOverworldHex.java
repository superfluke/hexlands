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

import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
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
import net.minecraftforge.fml.client.config.ConfigGuiType;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class ChunkGeneratorOverworldHex implements IChunkGenerator
{
	private final Random rand;
    final World world;
    Biome[] biomesForGeneration;
    protected static final IBlockState WATER = Blocks.WATER.getDefaultState();
    protected static final IBlockState STONE = Blocks.STONE.getDefaultState();
    

    protected Layout hex_layout = new Layout(Layout.flat, new Point(Configs.worldgen.hexSize, Configs.worldgen.hexSize), new Point(0, 0));

    private MapGenBase caveGenerator = new MapGenCaves();
    private MapGenStronghold strongholdGenerator = new MapGenStronghold();
    private MapGenVillage villageGenerator = new MapGenVillage();
    private MapGenMineshaft mineshaftGenerator = new MapGenMineshaft();
    private MapGenScatteredFeature scatteredFeatureGenerator = new MapGenScatteredFeature();
    //private MapGenBase ravineGenerator = new MapGenRavine();
    private MapGenBase ravineGenerator = new HexGenRavine();
    private StructureOceanMonument oceanMonumentGenerator = new StructureOceanMonument();
    private IBlockState rimBlock;
    //private WoodlandMansion woodlandMansionGenerator = new WoodlandMansion(this);
    public IBlockState rim2 = Block.getBlockFromName("minecraft:netherrack").getDefaultState();
    
    public double[] heightmap;
    public Biome[] biomemap;
    
    public ChunkGeneratorOverworldHex(final World world)
    {
        updateRimBlock();

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
        world.setSeaLevel(Configs.worldgen.seaLevel);
        this.rand = new Random(world.getSeed());
        heightmap = new double[256];
        biomemap = new Biome[256];
        
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
        
        if(Configs.worldgen.generateCaves)
        {
	        this.caveGenerator.generate(this.world, x, z, chunkprimer);
        }
        if(Configs.worldgen.generateRavines)
        {
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
    	this.generateBiomemap(chunkX*16, chunkZ*16);
    	this.generateHeightmap(chunkX*16, chunkZ*16);
    	
        for (int x = 0; x < 16; x++)
        {
            final int realX = x + chunkX * 16;
            
            for (int z = 0; z < 16; z++)
            {
                final int realZ = z + chunkZ * 16;
                
                //convert x,z to a hex cords (q,r)
                Hex hexy = hex_layout.pixelToHex(new Point(realX, realZ)).hexRound();
                
                //convert hex cords back to x,z to get center point
                Point center_pt =  hex_layout.hexToPixel(hexy);

                boolean isEdgeBlock = TestEdge.isEdge(new Point(realX, realZ), center_pt, hexy, Configs.worldgen.hexSize, Configs.worldgen.hexSize);
                boolean isHardEdge = false;

                Biome this_biome = this.biomemap[x + z * 16];
                boolean isWet = this_biome == Biomes.OCEAN || this_biome == Biomes.DEEP_OCEAN;
                
                double hex_noise = SimplexNoise.noise(center_pt.getX()/60, center_pt.getZ()/60);
                
                float biomeBaseHeightRaw = this_biome.getBaseHeight();
                float biomeVariation = this_biome.getHeightVariation();
                biomeVariation = biomeVariation * 0.6F + 0.1F;
                float biomeBaseHeight = (biomeBaseHeightRaw * 16.0F - 1.0F) / 36.0F; //forces range -2 to 2 into -1 to 1
                biomeBaseHeight = biomeBaseHeight/2 + 0.5F; // into range 0 to 1
               
                if(biomeBaseHeight < 0.5)//ease in
                	biomeBaseHeight = 16*biomeBaseHeight*biomeBaseHeight*biomeBaseHeight*biomeBaseHeight*biomeBaseHeight;
                else //ease out
                	biomeBaseHeight = 1+16*(--biomeBaseHeight)*biomeBaseHeight*biomeBaseHeight*biomeBaseHeight*biomeBaseHeight ;
                
                biomeBaseHeight = biomeBaseHeight * 2 - 0.95F; //back into range -1 to 1
                biomeBaseHeight = (float) Math.pow(Math.abs(biomeBaseHeight), (1-biomeBaseHeight)*(1-biomeBaseHeight));//push values near 0 higher without messing with extreme values too much
            	
                if (biomeBaseHeightRaw < 0) //put negative values back in their place after that last adjustment
            		biomeBaseHeight *= -1;
                
                /*one day ill learn how to debug like a real man... not today
                if (lastBaseHight != biomeBaseHeight)
                {
	                System.out.printf("biome: %s, preb: %f, postb: %f\n", this_biome.getBiomeName(), biomeBaseHeightRaw, biomeBaseHeight);
                }
                lastBaseHight = biomeBaseHeight;
                */
                
                
                
                //figure out if we need to draw a line between 2 hexes
                if(!isWet && !Configs.worldgen.outlineAll)
            	{
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

                			double inner_diagonal = Hex.sqr3 * ((Configs.worldgen.hexSize-(boundry_size+1))-testX); //CHANGE TO ACTUAL BOUNDRY SIZE
                			double inner_flat = Hex.sqr3 * ((Configs.worldgen.hexSize-boundry_size)/2);
                			
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

                			double inner_diagonal = Hex.sqr3 * ((Configs.worldgen.hexSize-(boundry_size+1))-testX); //CHANGE TO ACTUAL BOUNDRY SIZE
                			double inner_flat = Hex.sqr3 * ((Configs.worldgen.hexSize-boundry_size)/2);
                			
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
		                	}
	                	}
	                }
            	}

                //adjust height by noise
                int hex_height = (int)(Configs.worldgen.terrainBaseline + Configs.worldgen.biomeHeightAdjustment * biomeBaseHeight + Configs.worldgen.extraHexNoise
                		*hex_noise);
                int block_height = hex_height;
                
                if(isWet)
                {
                	hex_height -= 6;
                	block_height = hex_height;
                	//block_height += (int)((50 * noise)*(bVar));
                }
                else if(this_biome == Biomes.MESA_CLEAR_ROCK)
                {
                	this.heightmap[x + z * 16] = Math.round(this.heightmap[x + z * 16] * 16 * 10)/10;
                }
                
                if(Configs.worldgen.outlineAll)
                {
                	isHardEdge = isEdgeBlock;
                }
                
                if(!isHardEdge)
                {
                	//get distance to center point of hex, though this whole thing assumes equal width and height
                	int xdif = realX - center_pt.getX();
                	int zdif = realZ - center_pt.getZ();
                	double distance_from_origin = Math.sqrt(xdif*xdif+zdif*zdif);
                	double distance_ratio = distance_from_origin/Configs.worldgen.hexSize;
                	if (distance_ratio > 0.9)
                		distance_ratio = 0.9;

                	int block_desired_height = (int)Math.round((block_height + (Configs.worldgen.terrainHeight * this.heightmap[x + z * 16])*(biomeVariation)));
                	
                	//smooth out where the terrain wants to be with the height of the hex rim based on distance from center of hex
                	block_height = (int)Math.round((block_desired_height*(1-distance_ratio) + hex_height*distance_ratio));
                
                }

                if(block_height>255)
                	block_height = 255;
                else if(block_height<1)
                	block_height = 1;
                
                //set everything under height to stone
                for (int y = 0; y < block_height; y++)
                {
					if(Configs.worldgen.borderToBedrock && isHardEdge)
						primer.setBlockState(x, y, z, rimBlock);
					else
						primer.setBlockState(x, y, z, STONE);
                }
                
                if (block_height < 60)
                {
                	for (int y = block_height; y < Configs.worldgen.seaLevel; y++)
                		primer.setBlockState(x, y, z, WATER);
                	block_height = 60;
                }
                else
                {
					if(Configs.worldgen.borderToBedrock && isHardEdge)
						primer.setBlockState(x, block_height, z, rimBlock);
					else
						primer.setBlockState(x, block_height, z, STONE);
                }

            	/*
            	if(realX == center_pt.getX() && realZ == center_pt.getZ()) //delete me, adds nether rack to hex midpoint for testing
            		primer.setBlockState(x, block_height+1, z, rim2);
            	*/
                
            	if(Configs.worldgen.outlineAll || isHardEdge)
            	{
            		if (isEdgeBlock)
            			primer.setBlockState(x, block_height+1, z, rimBlock);
            	}
            	
             
            }
        }
    }
    
    private void generateBiomemap(int startX, int startZ) 
    {
		for(int x=0; x<16; x++)
		{
			for(int z=0; z<16; z++)
			{
				this.biomemap[x + z * 16] = this.world.getBiomeProvider().getBiome(new BlockPos(startX+x, 0, startZ+z));
			}
		}
		
	}

	public void generateHeightmap (int startX, int startZ)
    {
		for(int x=0; x<16; x++)
		{
			for(int z=0; z<16; z++)
			{
				double noise = SimplexNoise.noise(startX+x, startZ+z, 190, 190, 0.5, 2);
		        double noiser = SimplexNoise.noise(startX+x, startZ+z, 90, 90, 0.5, 4);
		        double noisyist = SimplexNoise.noise(startX+x, startZ+z, 50, 50, 0.5, 4);
		        noise += noiser*0.8 + noisyist * 0.25 + 0.1;
		        noise *= Math.abs(noise+0.5)*0.8;
		        
		        this.heightmap[x + z * 16] = noise;
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
        
        
        if (Configs.worldgen.generateStructures)
        {	
        	if (Configs.worldgen.generateMineshaft)
        		this.mineshaftGenerator.generateStructure(this.world, this.rand, chunkpos);
        	if (Configs.worldgen.generateVillage)
        		villageHere = this.villageGenerator.generateStructure(this.world, this.rand, chunkpos);
        	if (Configs.worldgen.generateStronghold)
        		this.strongholdGenerator.generateStructure(this.world, this.rand, chunkpos);
        	if (Configs.worldgen.generateTemples)
        		this.scatteredFeatureGenerator.generateStructure(this.world, this.rand, chunkpos);
        	if (Configs.worldgen.generateMonuments)
        		this.oceanMonumentGenerator.generateStructure(this.world, this.rand, chunkpos);
        	//this.woodlandMansionGenerator.generateStructure(this.world, this.rand, chunkpos);
        }

        if (Configs.worldgen.lakesGenerate && biome != Biomes.DESERT && biome != Biomes.DESERT_HILLS && !villageHere && this.rand.nextInt(Configs.worldgen.lakeRarity) == 0)
        {
	        if (net.minecraftforge.event.terraingen.TerrainGen.populate(this, this.world, this.rand, x, z, villageHere, net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.LAKE))
	        {
	            int i1 = this.rand.nextInt(16) + 8;
	            int j1 = this.rand.nextInt(256);
	            int k1 = this.rand.nextInt(16) + 8;
	            (new WorldGenLakes(Blocks.WATER)).generate(this.world, this.rand, blockpos.add(i1, j1, k1));
	        }
        }

        if (!villageHere && this.rand.nextInt(80 / 10) == 0)
        {
            if (net.minecraftforge.event.terraingen.TerrainGen.populate(this, this.world, this.rand, x, z, villageHere, net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.LAVA))
            {
                int i2 = this.rand.nextInt(16) + 8;
                int l2 = this.rand.nextInt(this.rand.nextInt(248) + 8);
                int k3 = this.rand.nextInt(16) + 8;

                if (l2 < this.world.getSeaLevel() || (this.rand.nextInt(80 / 8) == 0 && Configs.worldgen.lavaLakesGenerate))
                {
                    (new WorldGenLakes(Blocks.LAVA)).generate(this.world, this.rand, blockpos.add(i2, l2, k3));
                }
            }
        }


        if (net.minecraftforge.event.terraingen.TerrainGen.populate(this, this.world, this.rand, x, z, villageHere, net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.DUNGEON))
        {
            for (int j2 = 0; j2 < Configs.worldgen.dungeonCount; ++j2)
            {
                int i3 = this.rand.nextInt(16) + 8;
                int l3 = this.rand.nextInt(256);
                int l1 = this.rand.nextInt(16) + 8;
                (new WorldGenDungeons()).generate(this.world, this.rand, blockpos.add(i3, l3, l1));
            }
        }

        //biome.decorate(this.world, this.rand, new BlockPos(i, 0, j)); TODO uncomment
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
    	boolean flag = false;

        if (Configs.worldgen.generateStructures && Configs.worldgen.generateMonuments && chunkIn.getInhabitedTime() < 3600L)
        {
            flag |= this.oceanMonumentGenerator.generateStructure(this.world, this.rand, new ChunkPos(x, z));
        }

        return flag;
    }

    @Override
    public List<SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos)
    {
    	Biome biome = this.world.getBiome(pos);

        if (Configs.worldgen.generateStructures)
        {
            if (creatureType == EnumCreatureType.MONSTER && this.scatteredFeatureGenerator.isSwampHut(pos))
            {
                return this.scatteredFeatureGenerator.getMonsters();
            }

            if (creatureType == EnumCreatureType.MONSTER && Configs.worldgen.generateMonuments && this.oceanMonumentGenerator.isPositionInStructure(this.world, pos))
            {
                return this.oceanMonumentGenerator.getMonsters();
            }
        }

        return biome.getSpawnableList(creatureType);
    }

    @Override
    public BlockPos getNearestStructurePos(World worldIn, String structureName, BlockPos position, boolean findUnexplored)
    {
    	if (!Configs.worldgen.generateStructures)
        {
            return null;
        }
        else if ("Stronghold".equals(structureName) && this.strongholdGenerator != null)
        {
            return this.strongholdGenerator.getNearestStructurePos(worldIn, position, findUnexplored);
        }
        //else if ("Mansion".equals(structureName) && this.woodlandMansionGenerator != null)
        //{
            //return this.woodlandMansionGenerator.getNearestStructurePos(worldIn, position, findUnexplored);
        //}
        else if ("Monument".equals(structureName) && this.oceanMonumentGenerator != null)
        {
            return this.oceanMonumentGenerator.getNearestStructurePos(worldIn, position, findUnexplored);
        }
        else if ("Village".equals(structureName) && this.villageGenerator != null)
        {
            return this.villageGenerator.getNearestStructurePos(worldIn, position, findUnexplored);
        }
        else if ("Mineshaft".equals(structureName) && this.mineshaftGenerator != null)
        {
            return this.mineshaftGenerator.getNearestStructurePos(worldIn, position, findUnexplored);
        }
        else
        {
            return "Temple".equals(structureName) && this.scatteredFeatureGenerator != null ? this.scatteredFeatureGenerator.getNearestStructurePos(worldIn, position, findUnexplored) : null;
        }
    }

    @Override
    public void recreateStructures(Chunk chunkIn, int x, int z)
    {
    	if (Configs.worldgen.generateStructures)
        {
    		if (Configs.worldgen.generateMineshaft)
    			this.mineshaftGenerator.generate(this.world, x, z, (ChunkPrimer)null);
    		if (Configs.worldgen.generateVillage)
    			this.villageGenerator.generate(this.world, x, z, (ChunkPrimer)null);
    		if (Configs.worldgen.generateStronghold)
    			this.strongholdGenerator.generate(this.world, x, z, (ChunkPrimer)null);
    		if (Configs.worldgen.generateTemples)
    			this.scatteredFeatureGenerator.generate(this.world, x, z, (ChunkPrimer)null);
    		if (Configs.worldgen.generateMonuments)
    			this.oceanMonumentGenerator.generate(this.world, x, z, (ChunkPrimer)null);
        	//this.woodlandMansionGenerator.generate(this.world, x, z, (ChunkPrimer)null);
        }
        
    }

    @Override
    public boolean isInsideStructure(World worldIn, String structureName, BlockPos pos)
    {
        if (!Configs.worldgen.generateStructures)
        {
            return false;
        }
        else if ("Stronghold".equals(structureName) && this.strongholdGenerator != null)
        {
            return this.strongholdGenerator.isInsideStructure(pos);
        }
        //else if ("Mansion".equals(structureName) && this.woodlandMansionGenerator != null)
        //{
            //return this.woodlandMansionGenerator.isInsideStructure(pos);
        //}
        else if ("Monument".equals(structureName) && this.oceanMonumentGenerator != null)
        {
            return this.oceanMonumentGenerator.isInsideStructure(pos);
        }
        else if ("Village".equals(structureName) && this.villageGenerator != null)
        {
            return this.villageGenerator.isInsideStructure(pos);
        }
        else if ("Mineshaft".equals(structureName) && this.mineshaftGenerator != null)
        {
            return this.mineshaftGenerator.isInsideStructure(pos);
        }
        else
        {
            return "Temple".equals(structureName) && this.scatteredFeatureGenerator != null ? this.scatteredFeatureGenerator.isInsideStructure(pos) : false;
        }
    }

    public void updateRimBlock() {
    	String rimblock = Configs.worldgen.rimBlock;
    	if(rimblock.indexOf("@") > 0)
    	{
    		String[] metarim = rimblock.split("@");
    		rimBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(metarim[0])).getStateFromMeta(Integer.parseInt(metarim[1]));
    	}
    	else
    	{
    		rimBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(Configs.worldgen.rimBlock)).getDefaultState();
    	}
    }
}

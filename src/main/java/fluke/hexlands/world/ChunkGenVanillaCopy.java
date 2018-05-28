package fluke.hexlands.world;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import scala.actors.threadpool.Arrays;

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
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldEntitySpawner;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.MapGenCaves;
import net.minecraft.world.gen.MapGenRavine;
import net.minecraft.world.gen.NoiseGeneratorOctaves;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import net.minecraft.world.gen.feature.WorldGenDungeons;
import net.minecraft.world.gen.feature.WorldGenLakes;
import net.minecraft.world.gen.structure.MapGenMineshaft;
import net.minecraft.world.gen.structure.MapGenScatteredFeature;
import net.minecraft.world.gen.structure.MapGenStronghold;
import net.minecraft.world.gen.structure.MapGenVillage;
import net.minecraft.world.gen.structure.StructureOceanMonument;
import net.minecraft.world.gen.structure.WoodlandMansion;

public class ChunkGenVanillaCopy implements IChunkGenerator
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
    
    private NoiseGeneratorOctaves minLimitPerlinNoise;
    private NoiseGeneratorOctaves maxLimitPerlinNoise;
    private NoiseGeneratorOctaves mainPerlinNoise;
    private NoiseGeneratorPerlin surfaceNoise;
    public NoiseGeneratorOctaves scaleNoise;
    public NoiseGeneratorOctaves depthNoise;
    public NoiseGeneratorOctaves forestNoise;
    double[] mainNoiseRegion;
    double[] minLimitRegion;
    double[] maxLimitRegion;
    double[] depthRegion;
    private final double[] heightMap;
    private final float[] biomeWeights;
    
    public ChunkGenVanillaCopy(final World world)
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
        
        this.minLimitPerlinNoise = new NoiseGeneratorOctaves(this.rand, 16);
        this.maxLimitPerlinNoise = new NoiseGeneratorOctaves(this.rand, 16);
        this.mainPerlinNoise = new NoiseGeneratorOctaves(this.rand, 8);
        this.surfaceNoise = new NoiseGeneratorPerlin(this.rand, 4);
        this.scaleNoise = new NoiseGeneratorOctaves(this.rand, 10);
        this.depthNoise = new NoiseGeneratorOctaves(this.rand, 16);
        this.forestNoise = new NoiseGeneratorOctaves(this.rand, 8);
        this.heightMap = new double[825];
        this.biomeWeights = new float[25];
        for (int i = -2; i <= 2; ++i)
        {
            for (int j = -2; j <= 2; ++j)
            {
                float f = 10.0F / MathHelper.sqrt((float)(i * i + j * j) + 0.2F);
                this.biomeWeights[i + 2 + (j + 2) * 5] = f;
            }
        }
    }

    @Override
    public Chunk generateChunk(int x, int z)
    {
    	
        this.rand.setSeed((long)x * 341873128712L + (long)z * 132897987541L);
        ChunkPrimer chunkprimer = new ChunkPrimer();
        
        this.biomesForGeneration = this.world.getBiomeProvider().getBiomes(this.biomesForGeneration, x * 16, z * 16, 16, 16);
        //this.generateTerrain(x, z, chunkprimer);
        this.setBlocksInChunk(x, z, chunkprimer);
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
                
                //get noise at hex cords
                //double hex_noise = SimplexNoise.noise(hexy.q, hexy.r);
                double hex_noise = SimplexNoise.noise(center_pt.getX()/60, center_pt.getZ()/60);
                
                double block_noise = SimplexNoise.noise(realX, realZ, 60, 60, 0.5, 2); //[-1,1]
                double block_noiser = SimplexNoise.noise(realX, realZ, 40, 40, 0.5, 2)/2 + 0.5;//[0,1]
                boolean isEdgeBlock = TestEdge.isEdge(new Point(realX, realZ), center_pt, hexy, HEX_X_SIZE, HEX_Z_SIZE);
                boolean isHardEdge = false;
                Biome this_biome = this.world.getBiomeProvider().getBiome(new BlockPos(realX, 90, realZ));
                boolean isWet = this_biome == Biomes.OCEAN || this_biome == Biomes.DEEP_OCEAN;
                
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
		                	if(this_biome == neighbor_biome)
		                	{
		                		//isHardEdge = false; //turn off this flag so height adjusts normally
		                	}
		                	else
		                	{
		                		isHardEdge = true;
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
                
                //adjust height by noise
                //int height = (int)(60 + 14*(hex_noise+this_biome.getBaseHeight()));
                int hex_height = (int)(Configs.terrainBaseline + 16*(this_biome.getBaseHeight()));// + 3*hex_noise);
                int block_height = hex_height;
                if(isWet)
                {
                	hex_height -=6;
                	block_height = (int)(hex_height +  16*this_biome.getHeightVariation()*block_noiser);
                }
                else if(!isHardEdge)
                {
                	//get distance to center point of hex, though this whole thing assumes equal width and height
                	int xdif = realX - center_pt.getX();
                	int zdif = realZ - center_pt.getZ();
                	double distance_from_origin = Math.sqrt(xdif*xdif+zdif*zdif);
                	double distance_ratio = distance_from_origin/HEX_X_SIZE;
                	if (distance_ratio > 0.85)
                		distance_ratio = 0.85;
                	//int block_desired_height = (int)(hex_height + 5*block_noise + 32*this_biome.getHeightVariation()*block_noiser);
                	int block_desired_height = (int)(block_noiser * this_biome.getHeightVariation() * Configs.terrainHeight * this_biome.getBaseHeight() + 6 * block_noise) + Configs.terrainBaseline ;
                	
                	//smooth out where the terrain wants to be with the height of the hex rim based on distance from center of hex
                	block_height = (int)(block_desired_height*(1-distance_ratio) + hex_height*distance_ratio);
                	//System.out.printf("%d %d\n", block_desired_height, block_height);
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
                	for (int y = block_height; y < 60; y++)
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
    
    public void setBlocksInChunk(int x, int z, ChunkPrimer primer)
    {
        this.biomesForGeneration = this.world.getBiomeProvider().getBiomesForGeneration(this.biomesForGeneration, x * 4 - 2, z * 4 - 2, 10, 10);
        this.generateHeightmap(x * 4, 0, z * 4);
        System.out.println(Arrays.toString(this.heightMap));

        for (int i = 0; i < 4; ++i)
        {
            int j = i * 5;
            int k = (i + 1) * 5;

            for (int l = 0; l < 4; ++l)
            {
                int i1 = (j + l) * 33;
                int j1 = (j + l + 1) * 33;
                int k1 = (k + l) * 33;
                int l1 = (k + l + 1) * 33;

                for (int i2 = 0; i2 < 32; ++i2)
                {
                    double d0 = 0.125D;
                    double d1 = this.heightMap[i1 + i2];
                    double d2 = this.heightMap[j1 + i2];
                    double d3 = this.heightMap[k1 + i2];
                    double d4 = this.heightMap[l1 + i2];
                    double d5 = (this.heightMap[i1 + i2 + 1] - d1) * 0.125D;
                    double d6 = (this.heightMap[j1 + i2 + 1] - d2) * 0.125D;
                    double d7 = (this.heightMap[k1 + i2 + 1] - d3) * 0.125D;
                    double d8 = (this.heightMap[l1 + i2 + 1] - d4) * 0.125D;

                    for (int j2 = 0; j2 < 8; ++j2)
                    {
                        double d9 = 0.25D;
                        double d10 = d1;
                        double d11 = d2;
                        double d12 = (d3 - d1) * 0.25D;
                        double d13 = (d4 - d2) * 0.25D;

                        for (int k2 = 0; k2 < 4; ++k2)
                        {
                            double d14 = 0.25D;
                            double d16 = (d11 - d10) * 0.25D;
                            double lvt_45_1_ = d10 - d16;

                            for (int l2 = 0; l2 < 4; ++l2)
                            {
                                if ((lvt_45_1_ += d16) > 0.0D)
                                {
                                    primer.setBlockState(i * 4 + k2, i2 * 8 + j2, l * 4 + l2, STONE);
                                }
                                else if (i2 * 8 + j2 < 63) //this.settings.seaLevel)
                                {
                                    primer.setBlockState(i * 4 + k2, i2 * 8 + j2, l * 4 + l2, WATER);
                                }
                            }

                            d10 += d12;
                            d11 += d13;
                        }

                        d1 += d5;
                        d2 += d6;
                        d3 += d7;
                        d4 += d8;
                    }
                }
            }
        }
    }
    private void generateHeightmap(int p_185978_1_, int p_185978_2_, int p_185978_3_)
    {
//        this.depthRegion = this.depthNoise.generateNoiseOctaves(this.depthRegion, p_185978_1_, p_185978_3_, 5, 5, (double)this.settings.depthNoiseScaleX, (double)this.settings.depthNoiseScaleZ, (double)this.settings.depthNoiseScaleExponent);
        this.depthRegion = this.depthNoise.generateNoiseOctaves(this.depthRegion, p_185978_1_, p_185978_3_, 5, 5, 200D, 200D, 0.5);

//        float f = this.settings.coordinateScale;
//        float f1 = this.settings.heightScale;
        float f = 684F;
        float f1 = 684F;
//        this.mainNoiseRegion = this.mainPerlinNoise.generateNoiseOctaves(this.mainNoiseRegion, p_185978_1_, p_185978_2_, p_185978_3_, 5, 33, 5, (double)(f / this.settings.mainNoiseScaleX), (double)(f1 / this.settings.mainNoiseScaleY), (double)(f / this.settings.mainNoiseScaleZ));
        this.mainNoiseRegion = this.mainPerlinNoise.generateNoiseOctaves(this.mainNoiseRegion, p_185978_1_, p_185978_2_, p_185978_3_, 5, 33, 5, (double)(f / 80), (double)(f1 / 160), (double)(f / 80));

        this.minLimitRegion = this.minLimitPerlinNoise.generateNoiseOctaves(this.minLimitRegion, p_185978_1_, p_185978_2_, p_185978_3_, 5, 33, 5, (double)f, (double)f1, (double)f);
        this.maxLimitRegion = this.maxLimitPerlinNoise.generateNoiseOctaves(this.maxLimitRegion, p_185978_1_, p_185978_2_, p_185978_3_, 5, 33, 5, (double)f, (double)f1, (double)f);
        int i = 0;
        int j = 0;

        for (int k = 0; k < 5; ++k)
        {
            for (int l = 0; l < 5; ++l)
            {
                float f2BV = 0.0F;
                float f3BH = 0.0F;
                float f4 = 0.0F;
                int i1 = 2;
                Biome biome = this.biomesForGeneration[k + 2 + (l + 2) * 10];

                for (int j1 = -2; j1 <= 2; ++j1)
                {
                    for (int k1 = -2; k1 <= 2; ++k1)
                    {
                        Biome biome1 = this.biomesForGeneration[k + j1 + 2 + (l + k1 + 2) * 10];
//                        float f5 = this.settings.biomeDepthOffSet + biome1.getBaseHeight() * this.settings.biomeDepthWeight;
//                        float f6 = this.settings.biomeScaleOffset + biome1.getHeightVariation() * this.settings.biomeScaleWeight;
                        float biomeBaseHeight = 0 + biome1.getBaseHeight() * 1;
                        float biomeVariation = 0 + biome1.getHeightVariation() * 1;
                        float f7 = this.biomeWeights[j1 + 2 + (k1 + 2) * 5] / (biomeBaseHeight + 2.0F);

                        if (biome1.getBaseHeight() > biome.getBaseHeight())
                        {
                            f7 /= 2.0F;
                        }

                        f2BV += biomeVariation * f7;
                        f3BH += biomeBaseHeight * f7;
                        f4 += f7;
                    }
                }

                f2BV = f2BV / f4;
                f3BH = f3BH / f4;
                f2BV = f2BV * 0.9F + 0.1F;
                f3BH = (f3BH * 4.0F - 1.0F) / 8.0F;
                double d7 = this.depthRegion[j] / 8000.0D;

                if (d7 < 0.0D)
                {
                    d7 = -d7 * 0.3D;
                }

                d7 = d7 * 3.0D - 2.0D;

                if (d7 < 0.0D)
                {
                    d7 = d7 / 2.0D;

                    if (d7 < -1.0D)
                    {
                        d7 = -1.0D;
                    }

                    d7 = d7 / 1.4D;
                    d7 = d7 / 2.0D;
                }
                else
                {
                    if (d7 > 1.0D)
                    {
                        d7 = 1.0D;
                    }

                    d7 = d7 / 8.0D;
                }

                ++j;
                double d8BH = (double)f3BH;
                double d9BV = (double)f2BV;
                d8BH = d8BH + d7 * 0.2D;
//                d8 = d8 * (double)this.settings.baseSize / 8.0D;
//                double d0 = (double)this.settings.baseSize + d8 * 4.0D;
                d8BH = d8BH * 8.5 / 8.0D;
                double d0BH = 8.5 + d8BH * 4.0D;

                for (int l1 = 0; l1 < 33; ++l1)
                {
//                    double d1 = ((double)l1 - d0) * (double)this.settings.stretchY * 128.0D / 256.0D / d9;
                	double d1 = ((double)l1 - d0BH) * 12.0D * 128.0D / 256.0D / d9BV;

                    if (d1 < 0.0D)
                    {
                        d1 *= 4.0D;
                    }

//                    double d2 = this.minLimitRegion[i] / (double)this.settings.lowerLimitScale;
//                    double d3 = this.maxLimitRegion[i] / (double)this.settings.upperLimitScale;
                    double d2 = this.minLimitRegion[i] / 512D;
                    double d3 = this.maxLimitRegion[i] / 512D;
                    double d4 = (this.mainNoiseRegion[i] / 10.0D + 1.0D) / 2.0D;
                    double d5 = MathHelper.clampedLerp(d2, d3, d4) - d1;

                    if (l1 > 29)
                    {
                        double d6 = (double)((float)(l1 - 29) / 3.0F);
                        d5 = d5 * (1.0D - d6) + -10.0D * d6;
                    }

                    this.heightMap[i] = d5;
                    ++i;
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

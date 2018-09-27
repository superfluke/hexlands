package fluke.hexlands.world;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import fluke.hexlands.util.SimplexNoise;

//import com.bloodnbonesgaming.dimensionalcontrol.util.noise.OpenSimplexNoiseGeneratorOctaves;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.NoiseGeneratorOctaves;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import net.minecraftforge.event.terraingen.InitNoiseGensEvent.ContextOverworld;

//just a class for random tests
public class ChunkGeneratorOverworldCustom implements IChunkGenerator
{
    final Random rand = new Random();
    final World world;
    Biome[] biomesForGeneration;
    //protected final OpenSimplexNoiseGeneratorOctaves terrainNoise;
    //protected final SimplexNoise simnoise;
    
    private NoiseGeneratorOctaves minLimitPerlinNoise;
    private NoiseGeneratorOctaves maxLimitPerlinNoise;
    private NoiseGeneratorOctaves mainPerlinNoise;
    private NoiseGeneratorPerlin surfaceNoise;
    public NoiseGeneratorOctaves scaleNoise;
    public NoiseGeneratorOctaves depthNoise;
    public NoiseGeneratorOctaves forestNoise;
    
    double[] heightMap = null;
    float[] tittymonster = null;
    
    public ChunkGeneratorOverworldCustom(final World world)
    {
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
        ContextOverworld ctx = new net.minecraftforge.event.terraingen.InitNoiseGensEvent.ContextOverworld(minLimitPerlinNoise, maxLimitPerlinNoise, mainPerlinNoise, surfaceNoise, scaleNoise, depthNoise, forestNoise);
        ctx = net.minecraftforge.event.terraingen.TerrainGen.getModdedNoiseGenerators(world, this.rand, ctx);
    }

    @Override
    public Chunk generateChunk(int x, int z)
    {
        this.rand.setSeed((long)x * 341873128712L + (long)z * 132897987541L);
        ChunkPrimer chunkprimer = new ChunkPrimer();
        this.generateTerrain(x, z, chunkprimer);
        this.biomesForGeneration = this.world.getBiomeProvider().getBiomes(this.biomesForGeneration, x * 16, z * 16, 16, 16);

        

        Chunk chunk = new Chunk(this.world, chunkprimer, x, z);
        byte[] abyte = chunk.getBiomeArray();

        for (int i = 0; i < abyte.length; ++i)
        {
            abyte[i] = (byte)Biome.getIdForBiome(this.biomesForGeneration[i]);
        }

        chunk.generateSkylightMap();
        return chunk;
    }
    
    
    public void generateTerrain(final int chunkX, final int chunkZ, final ChunkPrimer primer)
    {
    	float prevBH = 0.001F;
        for (int x = 0; x < 16; x++)
        {
            final int realX = x + chunkX * 16;
            
            for (int z = 0; z < 16; z++)
            {
                final int realZ = z + chunkZ * 16;
                Biome this_biome = this.world.getBiomeProvider().getBiome(new BlockPos(realX, 90, realZ));
                float biomeBaseHeight = this_biome.getBaseHeight();
                float biomeVariation = this_biome.getHeightVariation();
                
                //delete me
                /*
                if (biomeBaseHeight != prevBH)
                	System.out.printf("biome: %s,  baseh: %f, vari: %f\n", this_biome.getBiomeName(), biomeBaseHeight, biomeVariation);
                prevBH = biomeBaseHeight;
                */
                
                float bVar = biomeVariation * 0.6F + 0.1F;
                float bBas = (biomeBaseHeight * 16.0F - 2.0F) / 8.0F;
                ///bBas *= 0.2;
                //bBas = bBas * 8.5F / 8.0F;
                bBas = 8.5F + bBas * 4.0F;
                //System.out.printf("biome: %s, bVar: %f,  bBas: %f\n", this_biome.getBiomeName(), bVar, bBas);
                
                //double noise3D = this.simnoise.noise(realX/60, realX/60, realZ/60);
                //double noise = Math.abs(this.simnoise.noise(realX, realZ, 1/2, 1/2, 0.5, 1));
                double noise = SimplexNoise.noise(realX, realZ, 100, 100, 0.5, 6);
                double noiser = SimplexNoise.noise(realX, realZ, 40, 40, 0.5, 2);
                double noisyist = SimplexNoise.noise(realX, realZ, 20, 20, 0.5, 2);
                noise += noiser*0.9 + noisyist * 0.6;
                noise *= Math.abs(noise+0.5)*0.8;

                int height = (int) (60 + 2 * bBas + (50 * noise)*(bVar));///(biomeVariation * 4 * 0.9F + 0.1F));
                if (height > 256)
                {
                	height = 256;
                }
                else if(height <1)
                {
                	height = 1;
                }
                
                for (int y = 0; y < height; y++)
                {
                    if (y <= height)
                    {
                        primer.setBlockState(x, y, z, Blocks.STONE.getDefaultState());
                    }
                }
                
                if (height < 128){
                	primer.setBlockState(x, height, z, Blocks.GRASS.getDefaultState());
                }
            }
        }
    }
	
    /*
    public void generateTerrain(final int chunkX, final int chunkZ, final ChunkPrimer primer)
    //public double[] func_4068_a(double ad[], int i, int j, int k, int l)
    {
        //if(ad == null || ad.length < k * l)
        //{
        //    ad = new double[k * l];
        //}
        //ad = field_4255_e.func_4101_a(ad, i, j, k, k, 0.02500000037252903D, 0.02500000037252903D, 0.25D);
        //this.depthRegion = this.depthNoise.generateNoiseOctaves(this.depthRegion, p_185978_1_, p_185978_3_, 5, 5, (double)this.settings.depthNoiseScaleX, (double)this.settings.depthNoiseScaleZ, (double)this.settings.depthNoiseScaleExponent);
        
        //field_4257_c = field_4253_g.func_4101_a(field_4257_c, i, j, k, k, 0.25D, 0.25D, 0.58823529411764708D);
        //int i1 = 0;
        /*
    	for(int j1 = 0; j1 < k; j1++)
        {
            for(int k1 = 0; k1 < l; k1++)
            {
                double d = field_4257_c[i1] * 1.1000000000000001D + 0.5D;
                double d1 = 0.01D;
                double d2 = 1.0D - d1;
                double d3 = (ad[i1] * 0.14999999999999999D + 0.69999999999999996D) * d2 + d * d1;
                d3 = 1.0D - (1.0D - d3) * (1.0D - d3);
                if(d3 < 0.0D)
                {
                    d3 = 0.0D;
                }
                if(d3 > 1.0D)
                {
                    d3 = 1.0D;
                }
                ad[i1] = d3;
                i1++;
            }

        }
		
        //return ad;
        */
    	
    	/*
    	double[] heightMap = null;
    	double[] noiseMain = null;
    	//heightMap = this.maxLimitPerlinNoise.generateNoiseOctaves(heightMap, chunkX*16, chunkZ*16, 16, 16, 80.0, 160.0, 80.0);
        //noiseMain = this.mainPerlinNoise.generateNoiseOctaves(noiseMain, chunkX*16, chunkZ*16, 16, 16, 8.0, 4.0, 8.0);
    	heightMap = this.surfaceNoise.getRegion(heightMap, (double)(chunkX * 16), (double)(chunkZ * 16), 16, 16, 0.25, 0.25, 0.25);
    	noiseMain = this.surfaceNoise.getRegion(noiseMain, (double)(chunkX * 16), (double)(chunkZ * 16), 16, 16, 0.25, 0.25, 0.59);
        int counter = 0;
        for (int x = 0; x < 16; x++)
        {
            final int realX = x + chunkX * 16;
            
            for (int z = 0; z < 16; z++)
            {
            	final int realZ = z + chunkZ * 16;
            	
            	double d = noiseMain[counter] * 1.1000000000000001D + 0.5D;
                double d1 = 0.01D;
                double d2 = 1.0D - d1;
                double d3 = (heightMap[counter] * 0.15 + 0.7) * d2 + d * d1;
                d3 = 1.0D - (1.0D - d3) * (1.0D - d3);
                if(d3 < 0.0D)
                {
                	System.out.printf("D3: %f, NM: %f, HM: %f\n", d3, noiseMain[counter], heightMap[counter]);
                    d3 = 0.0D;
                }
                if(d3 > 1.0D)
                {
                	System.out.println("tits");
                    d3 = 1.0D;
                }
                heightMap[counter] = d3;
                
                counter++;
            }
        }
        
    	
    	/*
    	if (heightMap == null)
        {
    		heightMap = new double[16*16*2*2*2*2];
        }
    	
    	
    	if (tittymonster == null)
        {
    		tittymonster = new float[25];
    		float fCPGInitNoiseBiomeNumerator = 10F;
    		float fCPGInitNoiseBiomeAdd = 0.2F;
            for (int k1 = -2; k1 <= 2; k1++)
            {
                for (int l1 = -2; l1 <= 2; l1++)
                {
                    float f = fCPGInitNoiseBiomeNumerator / MathHelper.sqrt((float)(k1 * k1 + l1 * l1) + fCPGInitNoiseBiomeAdd);
                    tittymonster[k1 + 2 + (l1 + 2) * 5] = f;
                }
            }
        }
        

        double xMajorScale = 684;  //d
        double yMajorScale = 684;  //d1
        double[] noise6 = null;
    	double[] noise3 = null;
    	double[] noise1 = null;
    	double[] noise2 = null;
    	//heightMap = this.maxLimitPerlinNoise.generateNoiseOctaves(heightMap, chunkX*16, chunkZ*16, 16, 16, 80.0, 160.0, 80.0);
        //noiseMain = this.mainPerlinNoise.generateNoiseOctaves(noiseMain, chunkX*16, chunkZ*16, 16, 16, 8.0, 4.0, 8.0);
        noise6 = depthNoise.generateNoiseOctaves(noise6, chunkX*16, chunkZ*16, 256, 256, 200.0, 200.0, 0.5);
        noise3 = mainPerlinNoise.generateNoiseOctaves(noise3, chunkX*16, chunkZ*16, 256, 256, xMajorScale / 80.0, yMajorScale / 160.0, xMajorScale / 80.0);
        noise1 = minLimitPerlinNoise.generateNoiseOctaves(noise1, chunkX*16, chunkZ*16, 256, 256, xMajorScale * 1.0, yMajorScale * 1.0, xMajorScale * 1.0);
        noise2 = maxLimitPerlinNoise.generateNoiseOctaves(noise2, chunkX*16, chunkZ*16, 256, 256, xMajorScale * 1.0, yMajorScale * 1.0, xMajorScale * 1.0);
        int i = 0;
        int k = 0;
        int l = 5;
        
        int i2 = 0;
        int j2 = 0;
        int i1 = 33;
        
        float fCPGInitNoiseBiomeMinHeightBump = 2.0F;

        for (int k2 = 0; k2 < 16; k2++)
        {
            for (int l2 = 0; l2 < l; l2++)
            {
                float f1 = 0.0F;
                float f2 = 0.0F;
                float f3 = 0.0F;
                byte byte0 = 2;
                int biomebase = k2 + 2 + (l2 + 2) * (l + 5);
                //Biome biomegenbase = biomesForGeneration[biomebase];
                Biome biomegenbase = Biomes.PLAINS;
                //this.biomesForGeneration = this.world.getBiomeProvider().getBiomes(this.biomesForGeneration, x * 16, z * 16, 16, 16);
                float biomeminheight = biomegenbase.getBaseHeight() - biomegenbase.getHeightVariation();
                for (int i3 = -byte0; i3 <= byte0; i3++)
                {
                    for (int j3 = -byte0; j3 <= byte0; j3++)
                    {
                    	int biomebase1 = k2 + i3 + 2 + (l2 + j3 + 2) * (l + 5);
                        //Biome biomegenbase1 = biomesForGeneration[biomebase1];
                    	Biome biomegenbase1 = Biomes.PLAINS;
                        float biome1minheight = biomegenbase1.getBaseHeight() - biomegenbase1.getHeightVariation();
                        
                        //float f4 = tittymonster[i3 + 2 + (j3 + 2) * 5] / (biomegenbase1.minHeight + mod_Wedge.CPGInitNoiseBiomeMinHeightBump);
                        float f4 = tittymonster[i3 + 2 + (j3 + 2) * 5] / (biome1minheight + fCPGInitNoiseBiomeMinHeightBump);
                        if (biome1minheight > biomeminheight)
                        {
                            //f4 /= mod_Wedge.CPGInitNoiseBiomeInterpFactor;
                        	f4 /= 2F;
                        }

                        f1 += biome1minheight * f4;
                        f2 += biome1minheight * f4;
                        f3 += f4;
                    }
                }

                f1 /= f3;
                f2 /= f3;
                //f1 = f1 * (1.0F - mod_Wedge.CPGInitNoiseSolidFloorFactor) + mod_Wedge.CPGInitNoiseSolidFloorFactor;
                f1 = f1 * (1.0F - 0.1F) + 0.1F;
                //f2 = (f2 * mod_Wedge.CPGInitNoiseF2Scale - mod_Wedge.CPGInitNoiseF2Sub) / mod_Wedge.CPGInitNoiseF2Denom;
                f2 = (f2 * 4F - 1.0F) / 8F;
                //double d2 = noise6[j2] / (double)mod_Wedge.CPGInitNoiseMidDenom;
                double d2 = noise6[j2] / 8000F;

                if (d2 < 0.0D)
                {
                    //d2 = -d2 * mod_Wedge.CPGInitNoiseD2ReverseFactor;
                	d2 = -d2 * 0.29999999999999999D;
                }

                //d2 = d2 * mod_Wedge.CPGInitNoiseD2Scale - mod_Wedge.CPGInitNoiseD2Subtract;
                d2 = d2 * 1D - 2D;

                if (d2 < 0.0D)
                {
                    //d2 /= mod_Wedge.CPGInitNoiseD2PreClampScale;
                	d2 /= 2D;

                    if (d2 < -1D)
                    {
                        d2 = -1D;
                    }
                    
                    //d2 /= mod_Wedge.CPGInitNoiseD2PostClampScale;
                    d2 /= 2.7999999999999998D;
                }
                else
                {
                    if (d2 > 1.0D)
                    {
                        d2 = 1.0D;
                    }

                    //d2 /= mod_Wedge.CPGInitNoiseD2NonClampScale;
                    d2 /= 8D;
                }

                j2++;

                for (int k3 = 0; k3 < i1; k3++)
                {
                    double d3 = f2;
                    double d4 = f1;
//                    d3 += d2 * mod_Wedge.CPGInitNoiseD2FinalScale;
                    d3 += d2 * 2D;
//                    d3 = (d3 * (double)i1) / mod_Wedge.CPGInitNoiseD3HeightFactor;
                    d3 = (d3 * (double)i1) / 16D;
//                    double d5 = (double)i1 / 2D + d3 * mod_Wedge.CPGInitNoiseD3BlockScale;
                    double d5 = (double)i1 / 2D + d3 * 4D;
                    double d6 = 0.0D;
//                    double d7 = (((double)k3 - d5) * mod_Wedge.CPGInitNoiseD7HeightScale * mod_Wedge.CPGInitNoiseD7HeightTotal) / 256D / d4;
                    double d7 = (((double)k3 - d5) * 12D * 128D) / 256D / d4;

                    if (d7 < 0.0D)
                    {
//                        d7 *= mod_Wedge.CPGInitNoiseD7ClampFactor;
                    	d7 *= 4D;
                    }

//                    double d8 = noise1[i2] / mod_Wedge.CPGInitNoiseLowerDenom;
                    double d8 = noise1[i2] / 512D;
//                    double d9 = noise2[i2] / mod_Wedge.CPGInitNoiseUpperDenom;
                    double d9 = noise2[i2] / 512D;
//                    double d10 = (noise3[i2] / mod_Wedge.CPGInitNoiseSlopeDenom + mod_Wedge.CPGInitNoiseSlopeAdd) * mod_Wedge.CPGInitNoiseSlopeScale;
                    double d10 = (noise3[i2] / 10D + 1.0D) * 0.5D;

                    if (d10 < 0.0D)
                    {
                        d6 = d8;
                    }
                    else if (d10 > 1.0D)
                    {
                        d6 = d9;
                    }
                    else
                    {
                        d6 = d8 + (d9 - d8) * d10;
                    }

                    d6 -= d7;

                    if (k3 > i1 - 4)
                    {
//                        double d11 = (double)(float)(k3 - (i1 - 4)) / mod_Wedge.CPGInitNoiseHeightLimitDenom;
                    	double d11 = (double)(float)(k3 - (i1 - 4)) / 3D;
//                        d6 = d6 * (1.0D - d11) + mod_Wedge.CPGInitNoiseHeightLimitRoundoff * d11;
                    	d6 = d6 * (1.0D - d11) + -10D * d11;
                    }

                    heightMap[i2] = d6;
                    i2++;
                }
            }
        }
        
        //System.out.println(Arrays.toString(heightMap));
        
        for (int x = 0; x < 16; x++)
        {
            final int realX = x + chunkX * 16;
            
            for (int z = 0; z < 16; z++)
            {
                final int realZ = z + chunkZ * 16;
                
                //final int height = 60;
                final int height = 60 + (int)(20*(heightMap[16*z+x]/100));
                
                for (int y = 0; y < 256; y++)
                {
                    if (y <= height)
                    {
                        primer.setBlockState(x, y, z, Blocks.STONE.getDefaultState()); 
                    }
                }
            }
        }
       
    }
     */
    
    @Override
    public void populate(int x, int z)
    {
        
        
    }

    @Override
    public boolean generateStructures(Chunk chunkIn, int x, int z)
    {
        
        return false;
    }

    @Override
    public List<SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos)
    {
        
        return null;
    }

    @Override
    public BlockPos getNearestStructurePos(World worldIn, String structureName, BlockPos position, boolean findUnexplored)
    {
        
        return null;
    }

    @Override
    public void recreateStructures(Chunk chunkIn, int x, int z)
    {
        
        
    }

    @Override
    public boolean isInsideStructure(World worldIn, String structureName, BlockPos pos)
    {
        
        return false;
    }
}

package fluke.hexlands.util.hex;

public class Point
{
    public Point(double x, double y)
    {
        this.x = x;
        this.y = y;
    }
    
    public int getX()
    {
    	return (int) this.x;
    }
    
    public int getY()
    {
    	return (int) this.y;
    }
    
    public int getZ()
    {
    	return (int) this.y;
    }
    
    public final double x;
    public final double y;
}

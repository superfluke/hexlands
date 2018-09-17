package fluke.hexlands.util.hex;

public class TestEdge {

	public static Boolean isEdge(Point pt, Point origin, Hex hex, int hex_x_size, int hex_z_size, int edgeSize)
	{
		int boundry_size = edgeSize;
		int x = Math.abs(pt.getX() - origin.getX());
		int z = Math.abs(pt.getZ() - origin.getZ());
		
		double inner_diagonal = Hex.sqr3 * ((hex_x_size-(boundry_size+1))-x);
		double inner_flat = Hex.sqr3 * ((hex_x_size-boundry_size)/2);
		double inner_boundry = Math.min(inner_diagonal, inner_flat);

		return z > inner_boundry;

	}
	
}

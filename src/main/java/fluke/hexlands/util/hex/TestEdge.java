package fluke.hexlands.util.hex;

public class TestEdge {

	public static Boolean isEdge(Point pt, Point origin, Hex hex, int hex_x_size, int hex_z_size)
	{
		
		//hex_size +=1;
		int boundry_size = 3;
		int x = Math.abs(pt.getX() - origin.getX());
		int z = Math.abs(pt.getZ() - origin.getZ());
		
		//double outer_diagonal = sqr3 * (hex_x_size-x);
		//double outer_flat = sqr3 * (hex_x_size/2);
		//double outer_boundry = Math.min(outer_diagonal, outer_flat);
		
		double inner_diagonal = Hex.sqr3 * ((hex_x_size-(boundry_size+1))-x);
		double inner_flat = Hex.sqr3 * ((hex_x_size-boundry_size)/2);
		double inner_boundry = Math.min(inner_diagonal, inner_flat);
		//System.out.printf("x, z: %d, %d, ox, oz: %d, %d, relx, relz: %d, %d, outerd: %f\n", pt.getX(), pt.getZ(), origin.getX(), origin.getZ(), x, z, outer_boundry);
		//return z < outer_boundry && z > inner_boundry;
		return z > inner_boundry;
		
		/*
		//new hey layout with smaller size
		Layout hex_layout = new Layout(Layout.flat, new Point(hex_x_size-3, hex_z_size-3), new Point(origin.getX(), origin.getZ()));
		
		//redraw hex at current point using smaller layout
		Hex testhex = hex_layout.pixelToHex(new Point(pt.getX(), pt.getZ())).hexRound();
		//draw a hex with a smaller size at the origin
		Hex innerhex = hex_layout.pixelToHex(new Point(origin.getX(), origin.getZ())).hexRound();
		
		//see if the current point is still inside the new origin hex, if not we are on an edge
		return innerhex.q != testhex.q || innerhex.r != testhex.r;
		*/
	}
	
}

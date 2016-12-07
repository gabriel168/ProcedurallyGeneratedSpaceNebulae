import javax.imageio.ImageIO;
import java.awt.image.AffineTransformOp;
import java.awt.geom.AffineTransform;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class SpaceNebulae {
	private static double[] getmin(double[][][] px){
		double[] mins = new double[px[0][0].length];
		for(int f = 0; f < px[0][0].length; f++){
			mins[f] = px[0][0][f];
		}
		for(int x = 0; x < px.length; x += 1){
            for(int y = 0; y < px[0].length; y += 1){
                for(int z = 0; z < px[0][0].length; z += 1){
                    mins[z] = Math.min(mins[z], px[x][y][z]);
                }
            }
        }
		return mins;
	}
	
	private static double[] getmax(double[][][] px) {
        double[] maxs = new double[px[0][0].length];
        for (int f = 0; f < px[0][0].length; f++) {
            maxs[f] = px[0][0][f];
        }

        for (int x = 0; x < px.length; x += 1) {
            for (int y = 0; y < px[0].length; y += 1) {
                for (int z = 0; z < px[0][0].length; z += 1) {
                    maxs[z] = Math.max(maxs[z], px[x][y][z]);
                }
            }
        }
        return maxs;
    }


    public static void main(String[] args){
        final int Width = 1920, Height = 1081;
       //final double scaleF = 2.0;
       //final int finW = (int) scaleF*Width, finH = (int) scaleF*Height;

        double[][][] pixels = new double[Width][Height][3];

        int seed = (int) (100*Math.random()); // => z-Offset
        double NoiseF = 1; //Stretch & Squeeze
        double DistNF = 4; // -------''------
        double DistortionScaleF = 3000;
        double NoiseScaleF = 1000;
        double NoiseOffset = 4;
        for(int y = 0; y < Height; y += 1){
            for(int x = 0; x < Width; x += 1){
            	for(int z = 0; z < pixels[0][0].length; z++) {
                    double xoff = ImprovedNoise.noise(DistNF * x / Width, DistNF * y / Height, z + seed);
                    double yoff = ImprovedNoise.noise(DistNF * x / Width, DistNF* y / Height, z + seed);
                    pixels[x][y][z] = NoiseOffset + NoiseScaleF * ImprovedNoise.noise((NoiseF*x + xoff*DistortionScaleF) / Width, (NoiseF * y+yoff*DistortionScaleF) / Height, z + seed);
                }
            }
        }

//DoublePX -> IntPx
        double[] min = getmin(pixels);
        double[] max = getmax(pixels);

        int[][][] intpx = new int[pixels.length][pixels[0].length][pixels[0][0].length];
        for(int y = 0; y < pixels[0].length; y += 1) {
            for (int x = 0; x < pixels.length; x += 1) {
                for (int z = 0; z < pixels[0][0].length; z += 1) {
                    intpx[x][y][z] = (int) (255 * (pixels[x][y][z] - min[z]) / (max[z] - min[z]));
                }
            }
        }




        BufferedImage pic = new BufferedImage(Width, Height, BufferedImage.TYPE_3BYTE_BGR);
        for(int y = 0; y < Height; y += 1){
            for(int x = 0; x < Width; x += 1){

            	Color pix = new Color(intpx[x][y][0], intpx[x][y][0], intpx[x][y][0]);
                int rgb = pix.getRGB();
                pic.setRGB(x, y, rgb);

            }
        }
        /*
        BufferedImage spic = new BufferedImage(finW, finH, BufferedImage.TYPE_3BYTE_BGR);
        AffineTransform at = new AffineTransform();
        at.scale(2.0, 2.0);
        AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BICUBIC);
        spic = scaleOp.filter(pic, spic);
        */


//OUTPUT
        File out = new File("pic.jpg");
        try {
            ImageIO.write(pic, "jpg", out);
        }catch(IOException e){
            e.printStackTrace();
        }
        System.out.println("Done");
    }
}

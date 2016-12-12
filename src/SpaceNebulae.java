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

    public static double[][][] mask(double[][][] px){
        double[][] ma = new double[px.length][px[0].length];
        double d = Math.sqrt(3)/2; //Perlin noise output range = +-d
        for (int x = 0; x < ma.length; x += 1) {
            for (int y = 0; y < ma[0].length; y += 1) {
                ma[x][y] = (px[x][y][2]+(d))/(d)-0.45;
            }
        }

        for(int i = 0; i < 10; i += 1) {
            ma = updatemask(ma);
        }

        for(int i = 0; i < 10; i += 1) {
        	ma = blurmask(ma);
        }

        for (int x = 0; x < ma.length; x += 1){
            for (int y = 0; y < ma[0].length; y += 1){
                for(int z = 0; z < px[0][0].length; z += 1){
                    px[x][y][z] = (1-ma[x][y])*(px[x][y][z]+2*d); 
                }
            }
        }

        return px;
    }

    public static double[][] updatemask(double[][] ma){
        double[][] ms = new double[ma.length][ma[0].length];
        for(int x = 0; x < ma.length; x += 1){
            for (int y = 0; y < ma[0].length; y += 1){
                try{
                    double s = ma[x-1][y-1] + ma[x-1][y] + ma[x-1][y+1]
                          + ma[x][y-1] + ma[x][y] + ma[x][y+1]
                          + ma[x+1][y-1] + ma[x+1][y] + ma[x+1][y+1];
                    if(s > 5){
                        ms[x][y] = 1;
                    }else{
                        ms[x][y] = 0;
                    }
                }catch(ArrayIndexOutOfBoundsException e){
                    ms[x][y] = 0;;
                }
            }
        }
        return ms;
    }

    public static double[][] blurmask(double[][] ma){
	double TOTAL;
	int fl = 10;
	
	for(int x = 0; x < ma.length; x += 1){
		TOTAL = 0;
		for(int b = 0; b < fl; b += 1){
			TOTAL += ma[x][b];
		}
		for(int y = fl; y < ma[0].length-fl; y += 1){
			TOTAL += ma[x][y+fl];
			TOTAL -= ma[x][y-fl];
			ma[x][y] = TOTAL/(2*fl+1);
		}
	}
	
	for(int y = 0; y < ma[0].length; y += 1){
		TOTAL = 0;
		for(int b = 0; b < fl; b += 1){
			TOTAL += ma[b][y];
		}
		for(int x = fl; x < ma.length-fl; x += 1){
			TOTAL += ma[x+fl][y];
			TOTAL -= ma[x-fl][y];
			ma[x][y] = TOTAL/(2*fl+1);
		}
	}
	
	return ma;
}
   
    public static void main(String[] args){
        final int Width = 1920, Height = 1080;
       //final double scaleF = 2.0;
       //final int finW = (int) scaleF*Width, finH = (int) scaleF*Height;

        double[][][] pixels = new double[Width][Height][3];

        int seed = (int) (100*Math.random()); // => z-Offset
        double NoiseF = 10; //Stretch & Squeeze
        double DistNF = 10; // -------''------
        double DistortionScaleF = 3000;

        for(int y = 0; y < Height; y += 1){
            for(int x = 0; x < Width; x += 1){
            	for(int z = 0; z < pixels[0][0].length; z++) {
                    double xoff = ImprovedNoise.noise(DistNF * x / Width, DistNF * y / Height, 2*seed+5);
                    double yoff = ImprovedNoise.noise(DistNF * x / Width, DistNF* y / Height, seed+2*z);
                    pixels[x][y][z] = ImprovedNoise.noise((NoiseF*x + xoff*DistortionScaleF) / Width, (NoiseF * y+yoff*DistortionScaleF) / Height, z + seed);

                }
            }
        }


        pixels = mask(pixels);

//DoublePX -> IntPx
        double[] min = getmin(pixels);
        double[] max = getmax(pixels);

        System.out.println("m: " + min[0] + ", M: " + max[0]);

        int[][][] intpx = new int[pixels.length][pixels[0].length][pixels[0][0].length];
        for(int y = 0; y < pixels[0].length; y += 1) {
            for (int x = 0; x < pixels.length; x += 1) {
                for (int z = 0; z < pixels[0][0].length; z += 1) {
                    intpx[x][y][z] = (int)(255 * (pixels[x][y][z] - min[z]) / (max[z] - min[z]));
                    //intpx[x][y][z] = (int)(255.0*Math.pow(((double)intpx[x][y][z]/255.0), 2));
                }
            }
        }

        BufferedImage pic = new BufferedImage(Width, Height, BufferedImage.TYPE_3BYTE_BGR);
        for(int y = 0; y < Height; y += 1){
            for(int x = 0; x < Width; x += 1){
            	Color pix = new Color(intpx[x][y][0], intpx[x][y][1], intpx[x][y][2]);
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
        File out = new File("pic.png");
        try {
            ImageIO.write(pic, "png", out);
        }catch(IOException e){
            e.printStackTrace();
        }
        System.out.println("Done");
    }
}

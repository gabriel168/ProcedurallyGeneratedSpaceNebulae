import javax.imageio.ImageIO;
import java.awt.image.AffineTransformOp;
import java.awt.geom.AffineTransform;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class SpaceNebulae {
	private static double[] getmin(double[][] px){
		double[] mins = new double[px[0].length];
		for(int f = 0; f < px[0].length; f++){
			mins[f] = px[0][f];
		}
		
		for(int f = 0; f < px.length; f++){
			for(int i = 0; i < px[0].length; i++){
				mins[i] = Math.min(mins[i], px[f][i]);
			}
		}
		return mins;
	}
	
	private static double[] getmax(double[][] px){
		double[] maxs = new double[px[0].length];
		for(int f = 0; f < px[0].length; f++){
			maxs[f] = px[0][f];
		}
		
		for(int f = 0; f < px.length; f++){
			for(int i = 0; i < px[0].length; i++){
				maxs[i] = Math.max(maxs[i], px[f][i]);
			}
		}
		return maxs;
	}
	
    public static void main(String[] args){
        final int Width = 720, Height = 480;
        final int finW = 1280, finH = 720;

        double[][] pixels = new double[Width*Height][3];

        int pos = 0;
        int seed = (int) (100*Math.random());
        for(int y = 0; y < Height; y += 1){
            for(int x = 0; x < Width; x += 1){
            	for(int z = 0; z < pixels[0].length; z++){
            		pixels[pos][z] = ImprovedNoise.noise(20.0*x / Width, 10.0*y / Height, (int) z+seed);
            	}
            	pos += 1;
            }
        }

        double[] min = getmin(pixels);
        double[] max = getmax(pixels);
        
        
        
        int[][] intpx = new int[pixels.length][pixels[0].length];
        for(int x = 0; x < pixels.length; x += 1){
        	for(int i = 0; i < 3; i += 1){
        		intpx[x][i] = (int) (255* (pixels[x][i] - min[i])/(max[i] - min[i]));
        	}
            
        }

        BufferedImage pic = new BufferedImage(Width, Height, BufferedImage.TYPE_3BYTE_BGR);
        pos = 0;
        for(int y = 0; y < Height; y += 1){
            for(int x = 0; x < Width; x += 1){
            	Color pix = new Color(intpx[pos][0], intpx[pos][1], intpx[pos][2]);
            	int rgb = pix.getRGB();
            	pic.setRGB(x, y, rgb);
            	pos += 1;
            }
        }

        
        BufferedImage spic = new BufferedImage(finW, finH, BufferedImage.TYPE_3BYTE_BGR);
       
        AffineTransform at = new AffineTransform();
        at.scale(2.0, 2.0);
        AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BICUBIC);
        spic = scaleOp.filter(pic, spic);
        
        
        File out = new File("pic3.jpg");
        try {
            ImageIO.write(spic, "jpg", out);
        }catch(IOException error){
            System.out.println("ups");
        }
        System.out.println("End");
    }
}

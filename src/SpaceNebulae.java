import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class SpaceNebulae {
    public static void main(String[] args){
        final int Width = 1280, Height = 720;

        double[] pixels = new double[Width*Height];

        int pos = 0;
        for(int y = 0; y < Height; y += 1){
            for(int x = 0; x < Width; x += 1){
                pixels[pos] = ImprovedNoise.noise(20.0*x / Width, 10.0*y / Height, 0);
                pos += 1;
            }
        }

        double min = pixels[0] , max = pixels[0];
        for(int x = 0; x < pixels.length; x += 1){
            min = Math.min(pixels[x], min);
            max = Math.max(pixels[x], max);
        }

        for(int x = 0; x < pixels.length; x += 1){
            pixels[x] = (int) (255* (pixels[x] -min)/(max -min));
        }

        BufferedImage pic = new BufferedImage(Width, Height, BufferedImage.TYPE_BYTE_GRAY);
        pic.getRaster().setPixels(0, 0, Width, Height, pixels);

        File out = new File("pic.jpg");
        try {
            ImageIO.write(pic, "jpg", out);
        }catch(IOException error){
            System.out.println("ups");
        }

    }
}

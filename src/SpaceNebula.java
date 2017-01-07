import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class SpaceNebula {
    private double[][][] pixel;
    private int[][][] intpx;
    private double[] min;
    private double[] max;

    public void checkMin() {
        for (int f = 0; f < this.pixel[0][0].length; f++) {
            this.min[f] = this.pixel[0][0][f];
        }
        for (int x = 0; x < this.pixel.length; x += 1) {
            for (int y = 0; y < this.pixel[0].length; y += 1) {
                for (int z = 0; z < this.pixel[0][0].length; z += 1) {
                    this.min[z] = Math.min(this.min[z], this.pixel[x][y][z]);
                }
            }
        }
    }

    public void checkMax() {
        for (int f = 0; f < this.pixel[0][0].length; f++) {
            this.max[f] = this.pixel[0][0][f];
        }

        for (int x = 0; x < this.pixel.length; x += 1) {
            for (int y = 0; y < this.pixel[0].length; y += 1) {
                for (int z = 0; z < this.pixel[0][0].length; z += 1) {
                    this.max[z] = Math.max(this.max[z], this.pixel[x][y][z]);
                }
            }
        }
    }


    public SpaceNebula(int width, int height){
        this.pixel = new double[width][height][3];
        this.min = new double[3];
        this.max = new double[3];
    }

    public void noiseFill() {
        int seed = (int) (100 * Math.random()); // => z-Offset
        double NoiseF = 50; //Stretch & Squeeze
        double DistNF = 50; // -------''------
        double DistortionScaleF = 1000;

        for (int x = 0; x < this.pixel.length; x += 1) {
            for (int y = 0; y < this.pixel[0].length; y += 1) {
                for (int z = 0; z < this.pixel[0][0].length; z++) {
                    double xoff = ImprovedNoise.noise(DistNF * x / this.pixel.length, DistNF * y / this.pixel[0].length, 2 * seed + 5);
                    double yoff = ImprovedNoise.noise(DistNF * x / this.pixel.length, DistNF * y / this.pixel[0].length, seed + 2 * z);
                    double xK = (NoiseF * x + xoff * DistortionScaleF) / this.pixel[0].length;
                    double yK = (NoiseF * y + yoff * DistortionScaleF) / this.pixel[0].length;
                    double zK = z + seed;
                    for (int o = 1; o < 16; o *= 2) {
                        this.pixel[x][y][z] += o * ImprovedNoise.noise(xK / o, yK / o, zK / o);
                        this.pixel[x][y][z] *= 1 + 0.75*Math.cos((yoff + xoff));
                    }
                }
            }
        }
    }

    public void schwaden() {
        double[][] mask = new double[this.pixel.length][this.pixel[0].length];
        for (int x = 0; x < mask.length; x += 1){
            for (int y = 0; y < mask[0].length; y += 1) {
                mask[x][y] = Math.round((this.pixel[x][y][0] - min[0]) / (max[0] - min[0]));
            }
        }

        for (int i = 0; i < 10; i += 1) {
            mask = updatemask(mask);
        }

        for (int i = 0; i < 2; i += 1) {
            mask = blurmask(mask);
        }

        this.checkMin();
        for (int x = 0; x < mask.length; x += 1) {
            for (int y = 0; y < mask[0].length; y += 1) {
                //if(!(0 <= mask[x][y] && mask[x][y] <= 1)){ System.out.println(mask[x][y]);}
                for (int z = 0; z < this.pixel[0][0].length; z += 1) {
                    this.pixel[x][y][z] = (mask[x][y]) * (this.pixel[x][y][z]-this.min[z]);
                }
            }
        }
    }

    private static double[][] updatemask(double[][] mask){
        double[][] newmask = new double[mask.length][mask[0].length];
        for (int x = 0; x < mask.length; x += 1) {
            for (int y = 0; y < mask[0].length; y += 1) {

                try {
                    double s = mask[x - 1][y - 1] + mask[x - 1][y] + mask[x - 1][y + 1]
                            + mask[x][y - 1] + mask[x][y] + mask[x][y + 1]
                            + mask[x + 1][y - 1] + mask[x + 1][y] + mask[x + 1][y + 1];
                    if (s > 5) {
                        newmask[x][y] = 1;
                    } else {
                        newmask[x][y] = 0;
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    newmask[x][y] = 0;
                    ;
                }
            }
        }
        return newmask;
    }



    private static double[][] blurmask(double[][] mask){
        double TOTAL;
        int fl = 7;
            for(int x = 0; x < mask.length; x += 1){
                TOTAL = 0;
                for(int b = 0; b < fl; b += 1){
                    TOTAL += mask[x][b];
                }
                for(int y = fl; y < mask[0].length-fl; y += 1){
                    TOTAL += mask[x][y+fl];
                    TOTAL -= mask[x][y-fl];
                    mask[x][y] = TOTAL/(2*fl+1);
                }
            }

            for(int y = 0; y < mask[0].length; y += 1){
                TOTAL = 0;
                for(int b = 0; b < fl; b += 1){
                    TOTAL += mask[b][y];
                }
                for(int x = fl; x < mask.length-fl; x += 1){
                    TOTAL += mask[x+fl][y];
                    TOTAL -= mask[x-fl][y];
                    mask[x][y] = TOTAL/(2*fl+1);
                }
            }
            return mask;
        }



    public void dimAround(int xC, int yC){
        this.checkMin();
        this.checkMax();
        for (int x = 0; x < this.pixel.length; x += 1){
            for (int y = 0; y < this.pixel[0].length; y += 1){
                for(int z = 0; z < this.pixel[0][0].length; z += 1){
                    double avgdydx = 0.5*(Math.abs(xC-x) + Math.abs(yC-y));
                    double dist = Math.sqrt(Math.pow(x-xC,2)+Math.pow(y-yC, 2));
                    this.pixel[x][y][z] = (this.pixel[x][y][z]-this.min[z])*Math.exp(-0.0025*(avgdydx+dist)-0.01*Math.abs(this.pixel[x][y][z]));
                    //this.pixel[x][y][z] *= Math.pow(Math.sin(0.001*dist+this.pixel[x][y][(z+1)%3]+this.pixel[x][y][z]+this.pixel[x][y][(z+2)%3]),10);
                    //http://lodev.org/cgtutor/randomnoise.html
                }
            }
        }
    }

    public void dimAround(){
        int xC = (int) ((this.pixel.length)*(Math.random()*0.5+0.25));
        int yC = (int) ((this.pixel[0].length)*(Math.random()*0.5+0.25));
        this.dimAround(xC, yC);
    }

    /** Taken from Earl F. Glynn's web page:
     * <a href="http://www.efg2.com/Lab/ScienceAndEngineering/Spectra.htm">Spectra Lab Report</a>
     * */
    public static int[] waveLengthToRGB(double Wavelength){
        double Gamma = 0.01;
        double IntensityMax = 255;

        double factor;
        double Red,Green,Blue;

        if((Wavelength >= 380) && (Wavelength<440)){
            Red = -(Wavelength - 440) / (440 - 380);
            Green = 0.0;
            Blue = 1.0;
        }else if((Wavelength >= 440) && (Wavelength<490)){
            Red = 0.0;
            Green = (Wavelength - 440) / (490 - 440);
            Blue = 1.0;
        }else if((Wavelength >= 490) && (Wavelength<510)){
            Red = 0.0;
            Green = 1.0;
            Blue = -(Wavelength - 510) / (510 - 490);
        }else if((Wavelength >= 510) && (Wavelength<580)){
            Red = (Wavelength - 510) / (580 - 510);
            Green = 1.0;
            Blue = 0.0;
        }else if((Wavelength >= 580) && (Wavelength<645)){
            Red = 1.0;
            Green = -(Wavelength - 645) / (645 - 580);
            Blue = 0.0;
        }else if((Wavelength >= 645) && (Wavelength<781)){
            Red = 1.0;
            Green = 0.0;
            Blue = 0.0;
        }else{
            Red = 0.0;
            Green = 0.0;
            Blue = 0.0;
        };

        // Let the intensity fall off near the vision limits

        if((Wavelength >= 380) && (Wavelength<420)){
            factor = 0.3 + 0.7*(Wavelength - 380) / (420 - 380);
        }else if((Wavelength >= 420) && (Wavelength<701)){
            factor = 1.0;
        }else if((Wavelength >= 701) && (Wavelength<781)){
            factor = 0.3 + 0.7*(780 - Wavelength) / (780 - 700);
        }else{
            factor = 0.0;
        };


        int[] rgb = new int[3];

        // Don't want 0^x = 1 for x <> 0
        rgb[0] = Red==0.0 ? 0 : (int) Math.round(IntensityMax * Math.pow(Red * factor, Gamma));
        rgb[1] = Green==0.0 ? 0 : (int) Math.round(IntensityMax * Math.pow(Green * factor, Gamma));
        rgb[2] = Blue==0.0 ? 0 : (int) Math.round(IntensityMax * Math.pow(Blue * factor, Gamma));

        return rgb;
    }

    public void toInt(){
        intpx = new int[pixel.length][pixel[0].length][pixel[0][0].length];
        this.checkMin();
        this.checkMax();
        for(int y = 0; y < pixel[0].length; y += 1) {
            for(int x = 0; x < pixel.length; x += 1) {
                for(int z = 0; z < pixel[0][0].length; z += 1) {
                    intpx[x][y][z] = (int)(255 * (pixel[x][y][z] - min[z]) / (max[z] - min[z]));
                }
            }
        }
    }

    public void monochromize(){
        int[] col = SpaceNebula.waveLengthToRGB(380+Math.random()*400);
        for(int x = 0; x < this.intpx.length; x += 1){
            for(int y = 0; y < this.intpx[0].length; y += 1){
                for(int z = 0; z < 3; z += 1){
                    this.intpx[x][y][z] = (int) (col[z]*intpx[x][y][z]/255.0);
                }
            }
        }
    }

    public void SternStunde(){
        this.SternStunde(50+(int)(Math.random()*100));
    }

    public void SternStunde(int Anzahl) {
        int a = 50, i = 200; //Helligkeit kleiner Sterne, aussen bzw innen
        for (int n = 0; n < Anzahl; n += 1) {
            int sx = (int) (Math.random() * this.intpx.length);
            int sy = (int) (Math.random() * this.intpx[0].length);
            if ((n) % 10 == 0 && intpx[sx][sy][0] + intpx[sx][sy][1] + intpx[sx][sy][2] < 30) {
                //Grosser Stern
                try {
                    int BigS = (int) (Math.random() * 30.0 + 10.0); //Grösse
                    double decF = Math.log(0.1 / 255.0) / BigS;
                    for (int x = -BigS; x < BigS; x += 1) {
                        for (int y = -BigS; y < BigS; y += 1) {
                            double dist = 0.5 * (Math.abs(x) + Math.abs(y));
                            for (int z = 0; z < this.intpx[0][0].length; z += 1) {
                                this.intpx[sx - x][sy - y][z] = (int) Math.max(this.intpx[sx - x][sy - y][z], (255.0 * Math.exp(decF * dist)));
                            }
                        }
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    //lol whatever
                }
            } else {
                //Kleiner Stern
                try {
                    for (int z = 0; z < this.intpx[0][0].length; z += 1) {
                        this.intpx[sx][sy][z] = 255;
                        this.intpx[sx + 1][sy][z] = i;
                        this.intpx[sx - 1][sy][z] = i;
                        this.intpx[sx][sy + 1][z] = i;
                        this.intpx[sx][sy - 1][z] = i;
						/*this.intpx[sx+1][sy+1][z] = a;
						this.intpx[sx+1][sy-1][z] = a;
						this.intpx[sx-1][sy+1][z] = a;
						this.intpx[sx-1][sy-1][z] = a;*/
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    //just give up lol
                }
            }
        }
    }

    public void writeToFile(String fileName){
        BufferedImage pic = new BufferedImage(intpx.length, intpx[0].length, BufferedImage.TYPE_3BYTE_BGR);
        for(int x = 0; x < intpx.length; x += 1){
            for(int y = 0; y < intpx[0].length; y += 1){
                int r = intpx[x][y][0];
                int g = intpx[x][y][1];
                int b = intpx[x][y][2];

                Color pix = new Color(r, g, b);
                int rgb = pix.getRGB();
                pic.setRGB(x, y, rgb);
            }
        }

        //OUTPUT
        File out = new File(fileName);
        try {
            ImageIO.write(pic, "png", out);
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        SpaceNebula nebel = new SpaceNebula(1920, 1080);
        nebel.noiseFill();
        nebel.schwaden();
        nebel.dimAround();
        nebel.toInt();
        nebel.monochromize();
        nebel.SternStunde(100);
        nebel.writeToFile("neb.png");
        System.out.println("Done");
    }
}


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

	public static double[][][] mask(double[][][] px, double[] min, double[] max){
		double[][][] ma = new double[px.length][px[0].length][px[0][0].length];

		for (int x = 0; x < ma.length; x += 1) {
			for (int y = 0; y < ma[0].length; y += 1) {
				for(int z = 0; z < ma[0][0].length; z += 1){
					ma[x][y][z] = Math.round((px[x][y][z]-min[z])/(max[z]-min[z]));
				}
			}
		}

		for(int i = 0; i < 10; i += 1) {
			ma = updatemask(ma);
		}

		for(int i = 0; i < 1; i += 1) {
			ma = blurmask(ma);
		}

		for (int x = 0; x < ma.length; x += 1){
			for (int y = 0; y < ma[0].length; y += 1){
				for(int z = 0; z < px[0][0].length; z += 1){
					//System.out.println(ma[x][y][z]);
					px[x][y][z] = (ma[x][y][(z+1)%3])*(px[x][y][z]);
				}
			}
		}

		return px;
	}

	public static double[][][] updatemask(double[][][] ma){
		double[][][] ms = new double[ma.length][ma[0].length][ma[0][0].length];
		for(int x = 0; x < ma.length; x += 1){
			for (int y = 0; y < ma[0].length; y += 1){
				for(int z = 0; z < ma[0][0].length; z += 1){
					try{
						double s = ma[x-1][y-1][z] + ma[x-1][y][z] + ma[x-1][y+1][z]
								+ ma[x][y-1][z] + ma[x][y][z] + ma[x][y+1][z]
										+ ma[x+1][y-1][z] + ma[x+1][y][z] + ma[x+1][y+1][z];
						if(s > 5){
							ms[x][y][z] = 1;
						}else{
							ms[x][y][z] = 0;
						}
					}catch(ArrayIndexOutOfBoundsException e){
						ms[x][y][z] = 0;;
					}
				}
			}
		}
		return ms;
	}

	public static double[][][] blurmask(double[][][] ma){
		double TOTAL;
		int fl = 5;
		for(int z = 0; z < ma[0][0].length; z += 1){
		for(int x = 0; x < ma.length; x += 1){
			TOTAL = 0;
			for(int b = 0; b < fl; b += 1){
				TOTAL += ma[x][b][z];
			}
			for(int y = fl; y < ma[0].length-fl; y += 1){
				TOTAL += ma[x][y+fl][z];
				TOTAL -= ma[x][y-fl][z];
				ma[x][y][z] = TOTAL/(2*fl+1);
			}
		}

		for(int y = 0; y < ma[0].length; y += 1){
			TOTAL = 0;
			for(int b = 0; b < fl; b += 1){
				TOTAL += ma[b][y][z];
			}
			for(int x = fl; x < ma.length-fl; x += 1){
				TOTAL += ma[x+fl][y][z];
				TOTAL -= ma[x-fl][y][z];
				ma[x][y][z] = TOTAL/(2*fl+1);
			}
		}
		}
		return ma;
	}

	public static double[][][] selectcenter(double[][][] px, double[] min){
		int xC, yC;

		xC = (int) ((px.length)*(Math.random()*0.5+0.25));
		yC = (int) ((px[0].length)*(Math.random()*0.5+0.25));

		for (int x = 0; x < px.length; x += 1){
			for (int y = 0; y < px[0].length; y += 1){
				for(int z = 0; z < px[0][0].length; z += 1){
					double avgdydx = 0.5*(Math.abs(xC-x) + Math.abs(yC-y));
					double dist = Math.sqrt(Math.pow(x-xC,2)+Math.pow(y-yC, 2));
					px[x][y][z] = (px[x][y][z]-min[z])*Math.pow(Math.exp(-0.0025*(avgdydx+dist)+0.05*px[x][y][z]),1);
					//px[x][y][z] *= Math.pow(Math.sin(0.01*dist+px[x][y][(z+1)%3]+px[x][y][z]-px[x][y][(z+2)%3]),10);
					//http://lodev.org/cgtutor/randomnoise.html
				}
			}
		}


		return px;
	}

	static private double Gamma = 0.01;
	static private double IntensityMax = 255;

	/** Taken from Earl F. Glynn's web page:
	 * <a href="http://www.efg2.com/Lab/ScienceAndEngineering/Spectra.htm">Spectra Lab Report</a>
	 * */
	public static int[] waveLengthToRGB(double Wavelength){
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

	public static int[][][] addStars(int[][][] px){
		int a = 50, i = 200; //Helligkeit kleiner Sterne, aussen bzw innen
		int N = 50 + (int) (Math.random()*120); //Anzahl Sterne


		for(int n = 0; n < N; n += 1){
			int sx = (int) (Math.random()*px.length);
			int sy = (int) (Math.random()*px[0].length);
			if((n)%10 == 0 && px[sx][sy][0]+px[sx][sy][1]+px[sx][sy][2] < 30){
				//Grosser Stern
				try{
					int BigS = (int) (Math.random()*30.0+10.0); //Grösse 
					double decF = Math.log(0.1/255.0)/BigS;
					for(int x = -BigS; x < BigS; x += 1){
						for(int y = -BigS; y < BigS; y += 1){
							double dist = 0.5*(Math.abs(x) + Math.abs(y));
							for(int z = 0; z < px[0][0].length; z += 1){
								px[sx-x][sy-y][z] = (int) Math.max(px[sx-x][sy-y][z], (255.0*Math.exp(decF*dist)));		
							}
						}
					}
				}catch(ArrayIndexOutOfBoundsException e){
					//lol whatever
				}
			}else{
				//Kleiner Stern
				try{
					for(int z = 0; z < px[0][0].length; z += 1){
						px[sx][sy][z] = 255;
						px[sx+1][sy][z] = i;
						px[sx-1][sy][z] = i;
						px[sx][sy+1][z] = i;
						px[sx][sy-1][z] = i;
						/*px[sx+1][sy+1][z] = a;
						px[sx+1][sy-1][z] = a;
						px[sx-1][sy+1][z] = a;
						px[sx-1][sy-1][z] = a;*/
					}
				}catch(ArrayIndexOutOfBoundsException e){
					//just give up lol
				}
			}
		}

		return px;
	}

	public static int[][][] monochromize(int[][][] px){
		int[] col = waveLengthToRGB(380+Math.random()*400);
		for(int x = 0; x < px.length; x += 1){
			for(int y = 0; y < px[0].length; y += 1){
				for(int z = 0; z < 3; z += 1){
					px[x][y][z] = (int) (col[z]*px[x][y][z]/255.0);
				}			
			}
		}
		return px;
	}

	public static void main(String[] args){
		final int Width = 1920, Height = 1080;

		double[][][] pixels = new double[Width][Height][3];

		int seed = (int) (100*Math.random()); // => z-Offset
		double NoiseF = 50; //Stretch & Squeeze
		double DistNF = 50; // -------''------
		double DistortionScaleF = 1000;

		for(int y = 0; y < Height; y += 1){
			for(int x = 0; x < Width; x += 1){
				for(int z = 0; z < pixels[0][0].length; z++) {	
					double xoff = ImprovedNoise.noise(DistNF * x / Width, DistNF * y / Height, 2*seed+5);
					double yoff = ImprovedNoise.noise(DistNF * x / Width, DistNF* y / Height, seed+2*z);
					double xK = (NoiseF*x + xoff*DistortionScaleF) / Width;
					double yK = (NoiseF * y+yoff*DistortionScaleF) / Height;
					double zK =  z + seed;
					pixels[x][y][z] = ImprovedNoise.noise(xK, yK, zK)+ 2*ImprovedNoise.noise(xK/2, yK/2, zK/2)+ 4*ImprovedNoise.noise(xK/4, yK/4, zK/4)+8*ImprovedNoise.noise(xK/8, yK/8, zK/8);
					//pixels[x][y][z] *= 1+0.5*Math.cos(yoff*xoff*5);
				}
			}
		}

		double[] min = getmin(pixels);
		double[] max = getmax(pixels);
		pixels = mask(pixels, min, max);
		min = getmin(pixels);
		pixels = selectcenter(pixels, min);

		//DoublePX -> IntPx
		min = getmin(pixels);
		max = getmax(pixels);

		System.out.println("m: " + min[0] + ", M: " + max[0]);

		int[][][] intpx = new int[pixels.length][pixels[0].length][pixels[0][0].length];
		for(int y = 0; y < pixels[0].length; y += 1) {
			for(int x = 0; x < pixels.length; x += 1) {
				for(int z = 0; z < pixels[0][0].length; z += 1) {
					intpx[x][y][z] = (int)(255 * (pixels[x][y][z] - min[z]) / (max[z] - min[z]));	
				}
			}
		}

		intpx = monochromize(intpx);
		intpx = addStars(intpx);

		//int[] col = waveLengthToRGB(380+Math.random()*400);
		BufferedImage pic = new BufferedImage(Width, Height, BufferedImage.TYPE_3BYTE_BGR);
		for(int y = 0; y < Height; y += 1){
			for(int x = 0; x < Width; x += 1){

				int r = intpx[x][y][0];//col[0]*intpx[x][y][0]/255;
				int g = intpx[x][y][1];//col[1]*intpx[x][y][0]/255;
				int b = intpx[x][y][2];//col[2]*intpx[x][y][0]/255;

				Color pix = new Color(r, g, b);
				int rgb = pix.getRGB();
				pic.setRGB(x, y, rgb);
			}
		}


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

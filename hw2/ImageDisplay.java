
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;

//import java.util.Arrays;

public class ImageDisplay 
{

	JFrame frame;
	JLabel lbIm1;
	BufferedImage imgOne;
	BufferedImage imgTwo;
	int width = 512;
	int height = 512;

	int len = width*height;
	byte[] bytes = new byte[(int) len*3];

	/** Read Image RGB
	 *  Reads the image of given width and height at the given imgPath into the provided BufferedImage.
	 */
	private void readImageRGB(int width, int height, String imgPath, BufferedImage img)
	{
		try
		{
			File file = new File(imgPath);
			RandomAccessFile raf = new RandomAccessFile(file, "r");
			raf.seek(0);

			raf.read(bytes);

			int ind = 0;
			for(int y = 0; y < height; y++)
			{
				for(int x = 0; x < width; x++)
				{
					byte a = 0;
					byte r = bytes[ind];
					byte g = bytes[ind+height*width];
					byte b = bytes[ind+height*width*2];

					int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
					//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
					img.setRGB(x,y,pix);
					ind++;
				}
			}
		}
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	// r,g,b values are from 0 to 1
	// h = [0,360], s = [0,1], v = [0,1]
	//		if s == 0, then h = -1 (undefined)
	private float[] RGBtoHSV(float r, float g, float b)
	{
		float[] hsv = new float[3];
		float h, s, v;

		float min, max, delta;
		min = Math.min( r, Math.min(g, b) );
		max = Math.max( r, Math.max(g, b) );
		v = max;				// v
		delta = max - min;
		if( max != 0 )
			s = delta / max;		// s
		else {
			// r = g = b = 0		// s = 0, v is undefined
			s = 0;
			h = -1;

			hsv[0] = h;
			hsv[1] = s;
			hsv[2] = v;
			return hsv;
		}
		if( r == max )
			h = ( g - b ) / delta;		// between yellow & magenta
		else if( g == max )
			h = 2 + ( b - r ) / delta;	// between cyan & yellow
		else
			h = 4 + ( r - g ) / delta;	// between magenta & cyan
		h *= 60;				// degrees
		if( h < 0 )
			h += 360;

		hsv[0] = h;
		hsv[1] = s;
		hsv[2] = v;
		return hsv;
	}
	private int HSVtoRGB( float h, float s, float v )
	{
		float r, g, b;
		int rgb;

		int i;
		float f, p, q, t;
		if( s == 0 ) {
			// achromatic (grey)
			r = g = b = v;
			rgb = 0xff000000 | (((int)r << 16) | ((int)g << 8) | (int)b );
			return rgb;
		}
		h /= 60;			// sector 0 to 5
		i = (int)Math.floor( h );
		f = h - i;			// factorial part of h
		p = v * ( 1 - s );
		q = v * ( 1 - s * f );
		t = v * ( 1 - s * ( 1 - f ) );
		switch( i ) {
			case 0:
				r = v;
				g = t;
				b = p;
				break;
			case 1:
				r = q;
				g = v;
				b = p;
				break;
			case 2:
				r = p;
				g = v;
				b = t;
				break;
			case 3:
				r = p;
				g = q;
				b = v;
				break;
			case 4:
				r = t;
				g = p;
				b = v;
				break;
			default:		// case 5:
				r = v;
				g = p;
				b = q;
				break;
		}

		rgb = 0xff000000 | (((int)r << 16) | ((int)g << 8) | (int)b );
		return rgb;
	}

	private void modify(int h1, int h2, BufferedImage img2)
	{

		int ind = 0;
		for(int i = 0; i < height; i++)
		{
			for(int j = 0; j < width; j++)
			{
				int r = (int)(bytes[ind] & 0xff);
				int g = (int)(bytes[ind+height*width] & 0xff);
				int b = (int)(bytes[ind+height*width*2] & 0xff);

				float[] hsv = RGBtoHSV((float)r, (float)g, (float)b);

				if(hsv[0] < h1 || hsv[0] > h2)
				{
					hsv[1] = 0; //set saturation to 0
				}

				int rgb_new = HSVtoRGB(hsv[0], hsv[1], hsv[2]); //24 bits a r g b

				//System.out.print((double)bytes[ind]+" ");
				//int pix = 0xff000000 | ((r_quan << 16) | (g_quan << 8) | b_quan );
				img2.setRGB(j,i,rgb_new);

				ind++;
			}
		}

	}

	public void showIms(String[] args)
	{

		// Read a parameter from command line
		int h1 = Integer.parseInt(args[1]);
		int h2 = Integer.parseInt(args[2]);

		// Read in the specified image
		imgOne = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		imgTwo = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		readImageRGB(width, height, args[0], imgOne);

		//Modify image
		modify(h1, h2, imgTwo);

		// Use label to display the image
		frame = new JFrame();
		GridBagLayout gLayout = new GridBagLayout();
		frame.getContentPane().setLayout(new FlowLayout());

		lbIm1 = new JLabel(new ImageIcon(imgOne));
		JLabel a = new JLabel(new ImageIcon(imgTwo));

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 1;
		frame.getContentPane().add(lbIm1, c);
		frame.getContentPane().add(a, c);

		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) 
	{
		ImageDisplay ren = new ImageDisplay();
		ren.showIms(args);
	}

}

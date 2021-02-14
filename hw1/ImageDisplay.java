
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
	int width = 352;
	int height = 288;

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

	private void modify(int Sy, int Su, int Sv, int Q, BufferedImage img2)
	{
		double[] y = new double[width*height];
		double[] u = new double[width*height];
		double[] v = new double[width*height];

		int sub_y_size = (len + Sy -1)/Sy;
		int sub_u_size = (len + Su -1)/Su;
		int sub_v_size = (len + Sv -1)/Sv; //8 subsample by 3 should take 3 samples

		double[] y_prime = new double[sub_y_size];
		double[] u_prime = new double[sub_u_size];
		double[] v_prime = new double[sub_v_size];

		//System.out.print("before conversion:");
		int ind = 0;
		for(int i = 0; i < height; i++)
		{
			for(int j = 0; j < width; j++)
			{
				double r = (double)(bytes[ind] & 0xff);
				double g = (double)(bytes[ind+height*width] & 0xff);
				double b = (double)(bytes[ind+height*width*2] & 0xff);

				y[ind] = 0.299*r + 0.587*g + 0.114*b;
				u[ind] = 0.596*r - 0.274*g - 0.322*b;
				v[ind] = 0.211*r - 0.523*g + 0.312*b;

				//System.out.print((double)bytes[ind]+" ");
				//System.out.print((double)bytes[ind+height*width]+" ");
				//System.out.print((double)bytes[ind+height*width*2]+" ");
				//if(ind < 10)
				//{
					//System.out.print(r+" ");
					//System.out.print(g+" ");
					//System.out.print(b);
					//System.out.print("\n");
				//}

				ind++;
			}
		}

		//subsample
		int y_ind = 0, u_ind = 0, v_ind = 0;
		for(int i=0; i < len; i++)
		{
			if (i%Sy == 0)
			{
				y_prime[y_ind] = y[i];
				y_ind++;
			}
			if (i%Su == 0)
			{
				u_prime[u_ind] = u[i];
				u_ind++;
			}
			if (i%Sv == 0)
			{
				v_prime[v_ind] = v[i];
				v_ind++;
			}
		}

		y_ind = 0;
		u_ind = 0;
		v_ind = 0;
		for (int i=0; i< len; i++)
		{
			if (i%Sy == 0)
			{
				y[i] = y_prime[y_ind];
				y_ind++;
			}
			else
			{
				y[i] = y_prime[y_ind - 1];
				if (y_ind < sub_y_size)
				{
					y[i] = (y_prime[y_ind - 1] + y_prime[y_ind])/2;
				}
			}

			if (i%Su == 0)
			{
				u[i] = u_prime[u_ind];
				u_ind++;
			}
			else
			{
				u[i] = u_prime[u_ind - 1];
				if (u_ind < sub_u_size)
				{
					u[i] = (u_prime[u_ind - 1] + u_prime[u_ind])/2;
				}
			}

			if (i%Sv == 0)
			{
				v[i] = v_prime[v_ind];
				v_ind++;
			}
			else
			{
				v[i] = v_prime[v_ind - 1];
				if (v_ind < sub_v_size)
				{
					v[i] = (v_prime[v_ind - 1] + v_prime[v_ind])/2;
				}
			}
		}

		//System.out.print("after conversion:");
		//convert back to rgb space, then quantization
		ind = 0;

		int interval = 256/Q;
		int[] possible_vals = new int[Q];
		for(int i = 0; i < Q; i++)
		{
			possible_vals[i] = i*interval;
		}
		//System.out.println(Arrays.toString(possible_vals));

		for(int i = 0; i < height; i++)
		{
			for(int j = 0; j < width; j++)
			{
				double r_prime = 1.000*y[ind] + 0.956*u[ind] + 0.621*v[ind];
				double g_prime = 1.000*y[ind] - 0.272*u[ind] - 0.647*v[ind];
				double b_prime = 1.000*y[ind] - 1.106*u[ind] + 1.703*v[ind];

				int nearst_r = Math.max(0, Math.min( Q-1, (int)Math.round(r_prime/interval) ) );
				int nearst_g = Math.max(0, Math.min( Q-1, (int)Math.round(g_prime/interval) ) );
				int nearst_b = Math.max(0, Math.min( Q-1, (int)Math.round(b_prime/interval) ) );

				int r_quan = possible_vals[nearst_r];
				int g_quan = possible_vals[nearst_g];
				int b_quan = possible_vals[nearst_b];

				int pix = 0xff000000 | ((r_quan << 16) | (g_quan << 8) | b_quan );
				img2.setRGB(j,i,pix);

				//if(ind < 10)
				//{
					//System.out.print(r_prime+" ");
					//System.out.print(g_prime+" ");
					//System.out.print(b_prime);
					//System.out.print("\n");
					//System.out.print(r_quan+" ");
					//System.out.print(g_quan+" ");
					//System.out.print(b_quan);
					//System.out.print("\n");
				//}

				ind++;
			}
		}

	}

	public void showIms(String[] args)
	{

		// Read a parameter from command line
		int Y = Integer.parseInt(args[1]);
		int U = Integer.parseInt(args[2]);
		int V = Integer.parseInt(args[3]);
		int Q = Integer.parseInt(args[4]);

		// Read in the specified image
		imgOne = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		imgTwo = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		readImageRGB(width, height, args[0], imgOne);

		//Modify image
		modify(Y,U,V,Q, imgTwo);

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

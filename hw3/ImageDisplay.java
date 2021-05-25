
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;

import java.lang.Math;
//import java.util.Arrays;

public class ImageDisplay 
{

	JFrame frame;
	JLabel lbIm1;
	BufferedImage imgOne;
	BufferedImage imgTwo;
	BufferedImage imgThree;
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

	private void show_matrix(double[][] matrix, int n, int m)
	{
		for(int i=0; i<n; i++)
		{
			for(int j=0; j<m; j++)
			{
				System.out.print(matrix[i][j] + " ");
			}
			System.out.print("\n");
		}
		System.out.print("\n");
	}

	private void modify(int coef_num, BufferedImage img2, BufferedImage img3)
	{

		int ind = 0;

		int [][] r = new int[height][width]; //512*512
		int [][] g = new int[height][width];
		int [][] b = new int[height][width];

		double [][] dct_r = new double[height][width];
		double [][] dct_g = new double[height][width];
		double [][] dct_b = new double[height][width];

		for(int i = 0; i < height; i++)
		{
			for(int j = 0; j < width; j++)
			{
				r[i][j] = (int)(bytes[ind] & 0xff);
				g[i][j] = (int)(bytes[ind+height*width] & 0xff);
				b[i][j] = (int)(bytes[ind+height*width*2] & 0xff);

				ind++;
			}
		}

		ind = 0;

		//DCT
		for(int u1 = 0; u1 < height; u1 += 8)
		{
			for(int v1 = 0; v1 < width; v1 += 8)
			{

				for(int u2 = 0; u2 < 8; u2++)
				{
					for(int v2 = 0; v2 < 8; v2++)
					{
						double cu, cv;
						int u, v;
						u = u1+u2;
						v = v1+v2;
						if(u%8 == 0)
						{
							cu = 1/Math.sqrt(2);
						}
						else
						{
							cu = 1;
						}
						if(v%8 == 0)
						{
							cv = 1/Math.sqrt(2);
						}
						else
						{
							cv = 1;
						}

						double after1 = 0;
						double after2 = 0;
						double after3 = 0;

						for(int x = u1; x < u1+8; x++)
						{
							for(int y = v1; y< v1+8; y++)
							{
								after1 += r[x][y]*Math.cos( (2*(x-u1)+1)*u2*Math.PI/16.0 ) *Math.cos( (2*(y-v1)+1)*v2*Math.PI/16.0) ;
								after2 += g[x][y]*Math.cos( (2*(x-u1)+1)*u2*Math.PI/16.0 ) *Math.cos( (2*(y-v1)+1)*v2*Math.PI/16.0) ;
								after3 += b[x][y]*Math.cos( (2*(x-u1)+1)*u2*Math.PI/16.0 ) *Math.cos( (2*(y-v1)+1)*v2*Math.PI/16.0) ;
							}
						}

						dct_r[u][v] = 0.25*cu*cv * after1;
						dct_g[u][v] = 0.25*cu*cv * after2;
						dct_b[u][v] = 0.25*cu*cv * after3;

					}
				}

			}
		}

		//IDCT
		int m = (int)Math.ceil(coef_num/4096.0);
		double[][] dct_r_p = new double[height][width];
		double[][] dct_g_p = new double[height][width];
		double[][] dct_b_p = new double[height][width];

		//System.out.print(after1);

		for(int i = 0; i < height; i++)
		{
			for(int j = 0; j < width; j++)
			{
				dct_r_p[i][j] = 0;
				dct_g_p[i][j] = 0;
				dct_b_p[i][j] = 0;
			}
		}

		for(int base_x = 0; base_x<height; base_x+=8)
		{
			for(int base_y = 0; base_y<width; base_y+=8)
			{
				int count = 0;
				for(int i = 0; i < 8*8; i++)
				{
					if(i%2 == 0)
					{
						int x = Math.min(i, 8-1);
						int y = i - x;
						while(x >= 0 && y <= 8-1)
						{
							dct_r_p[x+base_x][y+base_y] = dct_r[x+base_x][y+base_y];
							dct_g_p[x+base_x][y+base_y] = dct_g[x+base_x][y+base_y];
							dct_b_p[x+base_x][y+base_y] = dct_b[x+base_x][y+base_y];
							x--;
							y++;
							count++;
							if(count >= m)
							{
								break;
							}
						}
					}
					else
					{
						int y = Math.min(i, 8-1);
						int x = i - y;
						while(x <= 8-1 && y >= 0)
						{
							dct_r_p[x+base_x][y+base_y] = dct_r[x+base_x][y+base_y];
							dct_g_p[x+base_x][y+base_y] = dct_g[x+base_x][y+base_y];
							dct_b_p[x+base_x][y+base_y] = dct_b[x+base_x][y+base_y];
							x++;
							y--;
							count++;
							if(count >= m)
							{
								break;
							}
						}
					}
					if(count >= m)
					{
						break;
					}
				}
			}
		}

		//System.out.print(m + "\n");
		//show_matrix(dct_r_p, 16, 16);

		int[][] idct_r = new int[height][width];
		int[][] idct_g = new int[height][width];
		int[][] idct_b = new int[height][width];
		for(int x1 = 0; x1 < height; x1 += 8)
		{
			for(int y1 = 0; y1 < width; y1 += 8)
			{

				for(int x2 = 0; x2 < 8; x2++)
				{
					for(int y2 = 0; y2 < 8; y2++)
					{
						int x = x1+x2;
						int y = y1+y2;

						double after1 = 0;
						double after2 = 0;
						double after3 = 0;
						for(int u = x1; u < x1+8; u++)
						{
							for(int v = y1; v < y1+8; v++)
							{
								double cu, cv;
								if(u%8 == 0)
								{
									cu = 1/Math.sqrt(2);
								}
								else
								{
									cu = 1;
								}
								if(v%8 == 0)
								{
									cv = 1/Math.sqrt(2);
								}
								else
								{
									cv = 1;
								}
								after1 += cu*cv*dct_r_p[u][v]*Math.cos( (2*x2+1)*(u-x1)*Math.PI/16.0 )*Math.cos( (2*y2+1)*(v-y1)*Math.PI/16.0 );
								after2 += cu*cv*dct_g_p[u][v]*Math.cos( (2*x2+1)*(u-x1)*Math.PI/16.0 )*Math.cos( (2*y2+1)*(v-y1)*Math.PI/16.0 );
								after3 += cu*cv*dct_b_p[u][v]*Math.cos( (2*x2+1)*(u-x1)*Math.PI/16.0 )*Math.cos( (2*y2+1)*(v-y1)*Math.PI/16.0 );
							}
						}

						idct_r[x][y] = Math.max(Math.min((int)Math.round(0.25*after1), 255), 0);
						idct_g[x][y] = Math.max(Math.min((int)Math.round(0.25*after2), 255), 0);
						idct_b[x][y] = Math.max(Math.min((int)Math.round(0.25*after3), 255), 0);
					}
				}

			}
		}

		//DWT
		int size = 512;
		int reduce_size = 512;
		int target_size = 1;
		while(target_size*target_size < coef_num)
		{
			target_size *= 2;
		}
		if(target_size*target_size > coef_num)
		{
			target_size /= 2;
		}
		double[][] dwt_r = new double[height][width];
		double[][] dwt_g = new double[height][width];
		double[][] dwt_b = new double[height][width];

		double[][] dwt_r_p = new double[height][width];
		double[][] dwt_g_p = new double[height][width];
		double[][] dwt_b_p = new double[height][width];

		for(int i = 0; i<size; i++)
		{
			for(int j = 0; j<size; j++)
			{
				dwt_r[i][j] = r[i][j];
				dwt_g[i][j] = g[i][j];
				dwt_b[i][j] = b[i][j];
			}
		}
				
		while(reduce_size > target_size)
		{
			//gather lowpass in top left
			for(int i = 0; i<reduce_size; i++)
			{
				for(int j = 0; j<reduce_size; j+=2)
				{
					dwt_r_p[i][j/2] = (dwt_r[i][j]+dwt_r[i][j+1])/2;
					dwt_g_p[i][j/2] = (dwt_g[i][j]+dwt_g[i][j+1])/2;
					dwt_b_p[i][j/2] = (dwt_b[i][j]+dwt_b[i][j+1])/2;
				}

				for(int j = 0; j<reduce_size; j+=2)
				{
					dwt_r_p[i][j/2 + reduce_size/2] = (dwt_r[i][j]-dwt_r[i][j+1])/2;
					dwt_g_p[i][j/2 + reduce_size/2] = (dwt_g[i][j]-dwt_g[i][j+1])/2;
					dwt_b_p[i][j/2 + reduce_size/2] = (dwt_b[i][j]-dwt_b[i][j+1])/2;
				}
			}
			for(int i = 0; i<size; i++)
			{
				for(int j = 0; j<size; j++)
				{
					dwt_r[i][j] = dwt_r_p[i][j];
					dwt_g[i][j] = dwt_g_p[i][j];
					dwt_b[i][j] = dwt_b_p[i][j];
				}
			}

			for(int j = 0; j<reduce_size; j++)
			{
				for(int i = 0; i<reduce_size; i+=2)
				{
					dwt_r_p[i/2][j] = (dwt_r[i][j]+dwt_r[i+1][j])/2;
					dwt_g_p[i/2][j] = (dwt_g[i][j]+dwt_g[i+1][j])/2;
					dwt_b_p[i/2][j] = (dwt_b[i][j]+dwt_b[i+1][j])/2;
				}
				for(int i = 0; i<reduce_size; i+=2)
				{
					dwt_r_p[i/2 + reduce_size/2][j] = (dwt_r[i][j]-dwt_r[i+1][j])/2;
					dwt_g_p[i/2 + reduce_size/2][j] = (dwt_g[i][j]-dwt_g[i+1][j])/2;
					dwt_b_p[i/2 + reduce_size/2][j] = (dwt_b[i][j]-dwt_b[i+1][j])/2;
				}
			}
			for(int i = 0; i<size; i++)
			{
				for(int j = 0; j<size; j++)
				{
					dwt_r[i][j] = dwt_r_p[i][j];
					dwt_g[i][j] = dwt_g_p[i][j];
					dwt_b[i][j] = dwt_b_p[i][j];
				}
			}
			reduce_size = reduce_size/2;
		}

		//IDWT
		int selected_size = (int)Math.sqrt(coef_num);
		double [][] idwt_r = new double[height][width];
		double [][] idwt_g = new double[height][width];
		double [][] idwt_b = new double[height][width];
		for(int i = 0; i<size; i++)
		{
			for(int j = 0; j<size; j++)
			{
				if( i>= selected_size || j>=selected_size )
				{
					dwt_r[i][j] = 0;

					dwt_g[i][j] = 0;

					dwt_b[i][j] = 0;
				}
			}
		}
		int cur_size = target_size;
		while(cur_size < size)
		{
			for(int j = 0; j<cur_size*2; j++)
			{
				for(int i = 0; i<cur_size; i++)
				{
					idwt_r[2*i][j] = dwt_r[i][j] + dwt_r[i+cur_size][j];
					idwt_g[2*i][j] = dwt_g[i][j] + dwt_g[i+cur_size][j];
					idwt_b[2*i][j] = dwt_b[i][j] + dwt_b[i+cur_size][j];
				}
				for(int i = 0; i<cur_size; i++)
				{
					idwt_r[2*i+1][j] = dwt_r[i][j] - dwt_r[i+cur_size][j];
					idwt_g[2*i+1][j] = dwt_g[i][j] - dwt_g[i+cur_size][j];
					idwt_b[2*i+1][j] = dwt_b[i][j] - dwt_b[i+cur_size][j];
				}
			}
			for(int i = 0; i<size; i++)
			{
				for(int j = 0; j<size; j++)
				{
					dwt_r[i][j] = idwt_r[i][j];
					dwt_g[i][j] = idwt_g[i][j];
					dwt_b[i][j] = idwt_b[i][j];
				}
			}

			for(int i = 0; i<cur_size*2; i++)
			{
				for(int j = 0; j<cur_size; j++)
				{
					idwt_r[i][2*j] = dwt_r[i][j] + dwt_r[i][j+cur_size];
					idwt_g[i][2*j] = dwt_g[i][j] + dwt_g[i][j+cur_size];
					idwt_b[i][2*j] = dwt_b[i][j] + dwt_b[i][j+cur_size];
				}
				for(int j = 0; j<cur_size; j++)
				{
					idwt_r[i][2*j+1] = dwt_r[i][j] - dwt_r[i][j+cur_size];
					idwt_g[i][2*j+1] = dwt_g[i][j] - dwt_g[i][j+cur_size];
					idwt_b[i][2*j+1] = dwt_b[i][j] - dwt_b[i][j+cur_size];
				}
			}
			for(int i = 0; i<size; i++)
			{
				for(int j = 0; j<size; j++)
				{
					dwt_r[i][j] = idwt_r[i][j];
					dwt_g[i][j] = idwt_g[i][j];
					dwt_b[i][j] = idwt_b[i][j];
				}
			}
			cur_size *= 2;
		}

		for(int i = 0; i < height; i++)
		{
			for(int j = 0; j < width; j++)
			{
				idct_r[i][j] = Math.max(Math.min(idct_r[i][j], 255), 0);
				idct_g[i][j] = Math.max(Math.min(idct_g[i][j], 255), 0);
				idct_b[i][j] = Math.max(Math.min(idct_b[i][j], 255), 0);

				int pix2 = 0xff000000 | (((int)idct_r[i][j] << 16) | ((int)idct_g[i][j] << 8) | (int)idct_b[i][j] );
				int pix3 = 0xff000000 | (((int)dwt_r[i][j] << 16) | ((int)dwt_g[i][j] << 8) | (int)dwt_b[i][j] );
				img2.setRGB(j,i,pix2);
				img3.setRGB(j,i,pix3);
			}
		}

		//System.out.print(dct_r[0][0]);

	}

	public void showIms(String[] args)
	{

		// Read a parameter from command line
		int h1 = Integer.parseInt(args[1]);

		// Read in the specified image
		imgOne = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		imgTwo = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		imgThree = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		readImageRGB(width, height, args[0], imgOne);

		//Modify image
		modify(h1, imgTwo, imgThree);

		// Use label to display the image
		frame = new JFrame();
		GridBagLayout gLayout = new GridBagLayout();
		frame.getContentPane().setLayout(new BorderLayout());

		lbIm1 = new JLabel(new ImageIcon(imgOne));
		JLabel two = new JLabel(new ImageIcon(imgTwo));
		JLabel three = new JLabel(new ImageIcon(imgThree));
		JLabel text1 = new JLabel("original / DCT / DWT");

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 1;
		frame.getContentPane().add(text1, BorderLayout.PAGE_START);
		frame.getContentPane().add(lbIm1, BorderLayout.LINE_START);
		frame.getContentPane().add(two, BorderLayout.CENTER);
		frame.getContentPane().add(three, BorderLayout.LINE_END);

		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) 
	{
		ImageDisplay ren = new ImageDisplay();
		ren.showIms(args);
	}

}

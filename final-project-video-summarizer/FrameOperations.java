import java.util.*;
import java.util.List;
import java.nio.file.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import javax.imageio.ImageIO;
import javax.swing.*;

//Read in all frames from a folder and get the rgb values of each frame

public class FrameOperations {
	
	//import all frames from a folder and put into a map
	public List<File> getFrameList(List<File> fileList) {
		int width = 320; int height = 180;
		Map<Integer, double[][]> frames = new TreeMap<Integer, double[][]>();
		//get the name of all files in a file list
		Collections.sort(fileList,new FileNameComparator());   
		return fileList;
	}
	
	
	public double[][] getRGBvalues(int width, int height, File file) {
		double[][] frame = new double[180][320];
		try{
			int frameLength = width*height*3;
//			File file = new File(imgPath);
			RandomAccessFile raf = new RandomAccessFile(file, "r");
			//BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			raf.seek(0);
			long len = frameLength;
			byte[] bytes = new byte[(int) len];
			raf.read(bytes);
			int ind = 0;
			for(int x = 0; x < height; x++){
				for(int y = 0; y< width; y++){
					byte a = 0;
					byte r = bytes[ind];
					byte g = bytes[ind+height*width];
					byte b = bytes[ind+height*width*2];
					int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
					//img.setRGB(y,x,pix); 
					//int pixel = img.getRGB(y,x);
					frame[x][y] = pix;
					ind++;
				}
			}
			raf.close();
		}
		catch (FileNotFoundException e){
			e.printStackTrace();
		}
		catch (IOException e){
			e.printStackTrace();
		}
		return frame;
	}
	
	
	
	
	
	public class FileNameComparator implements Comparator<File> {
	    @Override
	    public int compare(File o1, File o2) {             
	        if (o1.getName().length()!=o2.getName().length()) {
	            return o1.getName().length()-o2.getName().length(); //overflow impossible since lengths are non-negative
	        }
	        return o1.compareTo(o2);
	    }
	}
}



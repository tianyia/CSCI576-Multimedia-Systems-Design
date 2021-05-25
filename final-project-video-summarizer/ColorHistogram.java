import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.*;
import java.util.List;
import java.nio.file.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

import javax.swing.*;

//        public histogramCompute(int[][][] histogram) {
//
//
//		try {
//			int offset = 0;
//			int numRead = 0;
//			while (offset < bytes.length
//					&& (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
//				offset += numRead;
//			}

public class ColorHistogram {

//    public static int WIDTH = 320;
//    public static int HEIGHT = 180;

    public static void ColorHistogram () {}

    public int[][][] getFrameHist(int WIDTH, int HEIGHT, File file){
        int[][][] histogram = new int[4][4][4];
        double rr = 0.0 ;
        double gg = 0.0 ;
        double bb = 0.0 ;
        long FRAMESIZE = WIDTH*HEIGHT*(24/8);
        int len = (int) FRAMESIZE;
        byte[] bytes = new byte[(int)len];

        int ind = 0;
        for(int y = 0; y < HEIGHT; y++)
        {

            for(int x = 0; x < WIDTH; x++)
            {

                byte a = 0;
                byte r = bytes[ind];
                byte g = bytes[ind+HEIGHT*WIDTH];
                byte b = bytes[ind+HEIGHT*WIDTH*2];

                int ri = (int)( (r & 0xff) &0xC0);
                int gi = (int)( (g & 0xff) &0xC0);
                int bi = (int)( (b & 0xff) &0xC0);

                histogram[ri/64][gi/64][bi/64]++;
                ind++;

            }
        }
        return histogram;
    }

//        byte[] bytes = new byte[(int)len];
////        double yAtPixel[][] = new double [WIDTH][HEIGHT];
//
//        int ind = 0;
//        for(int y = 0; y < HEIGHT; y++)
//        {
//
//            for(int x = 0; x < WIDTH; x++)
//            {
//
//                byte a = 0;//This is opacity value
//                byte r = bytes[ind];
//                byte g = bytes[ind+HEIGHT*WIDTH];
//                byte b = bytes[ind+HEIGHT*WIDTH*2];
//


}

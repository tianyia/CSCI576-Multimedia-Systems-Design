import java.util.*;
import java.io.File;
import java.math.*;
public class MotionVector {
	FrameOperations op = new FrameOperations();
	public static int width = 320;
	public static int height = 180;
	
	//Find motion vector of each consecutive frames
	//colocated macroblock size of 16x16
	//search parameter k = 8 for calculate efficiency
	//motion vector(i, j) where -8<=i, j<=8
	
	/**
	 * input: double[][] frame1, double[][] frame2, where frame1[x][y] indicates the rgb value of pixel(y,x)
	 * */
	public Vector<Double> CalculateMV(double[][] frame1, double[][] frame2) {
		//fixed size of frame is 320*180, therefore the center is (160, 90)
		//macroblock coordinates: (152,82)(168,82)(152,98)(168,98)
		Vector<Double> mv = new Vector<>();
		double[][] valueOfEachMV = new double[17][17]; //-8 <= i,j <= 8
		double abs = 0;
		for(int x = 0; x < 16; x++) {
			for(int y = 0; y < 16; y++) {
				for(int i = -8; i <= 8; i++) {
					for(int j = -8; j <= 8; j++) {
						valueOfEachMV[i+8][j+8] += Math.abs(frame1[152+x][82+y] - frame2[152+x+i][82+y+j]);			
					}
				}
			}
		}
		//find the min motion vector(i,j)
		double min = valueOfEachMV[0][0];
		double mvI = 0;
		double mvJ = 0;
		for(int i = 0; i < 17; i++) {
			for(int j = 0; j < 17; j++) {
				if(valueOfEachMV[i][j] < min) {
					min = valueOfEachMV[i][j];
					mvI = i - 8; mvJ = j - 8;
				}
			}
		}
		mv.add(mvI); mv.add(mvJ);
		return mv;
	}
	
	/**
	 * 
	 * @param filelist list of all frame files from one folder
	 * @return a list of index of key frames
	 */
	public ArrayList<Integer> findKeyFrames(List<File> list){
		ArrayList<Integer> keyFrames = new ArrayList<Integer>();
		for(int i = 0; i < list.size()-1; i++) {
			File file1 = list.get(i);
			File file2 = list.get(i+1);
			double[][] frame1 = op.getRGBvalues(width, height, file1);
			double[][] frame2 = op.getRGBvalues(width, height, file2);
			//calculate motion vectors of every consecutive frames
			Vector<Double> mv = CalculateMV(frame1, frame2);

			//if start of frames
			if(i == 0) {
				keyFrames.add(i);
			}
			//if the absolute value of both motion vector >= 1
			if(Math.abs(mv.get(0)) > 1 && Math.abs(mv.get(1)) > 1) {
				keyFrames.add(i+1);
			}
		}
		return keyFrames;
	}
	
	/**
	 * 
	 * @param indexStart index of start frame
	 * @param indexEnd index of end frame
	 * @param list list of all frame files
	 * @return motion vector score of a shot start from indexStart to indexEnd
	 */
	public double findShotScoreMV(int indexStart, int indexEnd, List<File> list) {
		double score = 0;
		//find the motion vector score of a shot
		for(int i = indexStart; i < indexEnd ; i++) {
			File file1 = list.get(i);
			File file2 = list.get(i+1);
			double[][] frame1 = op.getRGBvalues(width, height, file1);
			double[][] frame2 = op.getRGBvalues(width, height, file2);
			Vector<Double> mv = CalculateMV(frame1, frame2);
			score = score + Math.abs(mv.get(0)) + Math.abs(mv.get(1));
		}
		return score;
	}
}

import java.util.*;
import java.io.File;
import java.math.*;

public class HistogramScore {
    ColorHistogram hist = new ColorHistogram();
    public static int width = 320;
    public static int height = 180;
    private int[][][] histogramPrev;
    private int[][][] histogramNext;

    public double findShotScoreHist(int indexStart, int indexEnd, List<File> list) {
        double score = 0;
        double sum = 0;
        histogramPrev = new int[4][4][4];
        histogramNext = new int[4][4][4];

//        for(int i = 0; i < 4; i++)
//            for(int j = 0; j < 4; j++)
//                for(int k = 0; k < 4; k++){
//                    sum += Math.abs(histogramPrev[i][j][k] - histogramNext[i][j][k]);
//                }
        for(int x = indexStart; x < indexEnd ; x++) {
            File file1 = list.get(x);
            File file2 = list.get(x+1);
            histogramPrev = hist.getFrameHist(width, height, file1);
            histogramNext = hist.getFrameHist(width, height, file2);
            for(int i = 0; i < 4; i++)
                for(int j = 0; j < 4; j++)
                    for(int k = 0; k < 4; k++){
                        sum += Math.abs(histogramPrev[i][j][k] - histogramNext[i][j][k]);
                    }
        }
        return sum;
    }
}

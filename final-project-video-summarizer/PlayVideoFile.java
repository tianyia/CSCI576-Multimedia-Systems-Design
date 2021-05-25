import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.util.ArrayList;

public class PlayVideoFile extends Thread{
	AtomicBoolean paused;
	JFrame frame;
	JLabel outputImg;
	BufferedImage inputImg;
	GridBagConstraints c;
	List<File> fileList;
	String path;
	ArrayList<Integer> frameList;

	
	/**
	 * 
	 * @param paused if the thread got paused
	 * @param frame JFrame to add image on it
	 * @param outputImg JLabel to add image on it
	 * @param c GridBagConstraints to keep the size of whole frame
	 * @param path Path where to look for frames to display from
	 */
	public PlayVideoFile(AtomicBoolean paused, JFrame frame, JLabel outputImg, GridBagConstraints c,  String path, ArrayList<Integer> frameList) {
		this.paused = paused;
		this.frame = frame;
		this.outputImg = outputImg;
		this.c = c;
		this.path = path;
		File folder = new File(path);
	    List<File> fileList = Arrays.asList(folder.listFiles());
		this.fileList = fileList;
		this.frameList = frameList;
	}
	
	public void run() {
		//import from folder
		InputStream inputstream = null; //int i = 0; i < 10000; i++
		for(Integer i : frameList) {
			//if press the pause button
			if(paused.get()){
				synchronized(this){
					// Pause
					try{
						this.wait();
					} 
					catch (InterruptedException e) {
					}
				}
			}
			//during displaying
			try {
				//read in image
				inputstream = new FileInputStream(path + "/frame" + i + ".jpg");
				System.out.println(path + "/frame" + i + ".jpg");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			try {
				inputImg = ImageIO.read(inputstream);
			} catch (IOException e) {
				System.out.println("not able to read : " + i);
				e.printStackTrace();
			}
			//display this frame
			outputImg.setIcon(new ImageIcon(inputImg));	
			frame.getContentPane().add(outputImg, c);
			frame.pack();
			//frame.setVisible(true);
			try{
				//set a delay of 20 millisecond between frames
				Thread.sleep(27);
			}catch(InterruptedException ex){
				 System.out.println("well");
			}
		}
    }	
}


import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * plays a wave file using PlaySound class
 * 
 * @author Giulio
 */
public class playerTest {
	private static AtomicBoolean paused;
	private static Thread layoutThread;
	private static Thread videoThread;
	private static Thread audioThread;
	static JFrame frame;
	static JLabel outputImg;
	static JPanel panel;
	static JButton button;
	static GridBagConstraints labelConstraints;
	static GridBagConstraints buttonConstraints;
	static BufferedImage inputImg;
	public static int width = 320;
	public static int height = 180;
	public static ArrayList<Integer> frameList;

    /**
     * <Replace this with one clearly defined responsibility this method does.>
     * 
     * @param args
     *            the name of the wave file to play
     */
    public static void main(String[] args) {
    	// String title = "meridian"; // the title to find the folder we need for summarizing and playing the test
    	// String audioName = "../project_dataset/audio/"+title+".wav"; //create the path and audio name from title
    	// String videoFrameFolder = "../project_dataset/frames/" + title;

		String folder = args[0];
		String title = args[1]; // the title to find the folder we need for summarizing and playing the test
    	String audioName = "../" + folder + "/" + title + ".wav"; //create the path and audio name from title
    	String videoFrameFolder = "../" + folder + "/frames_test/" + title;
    	String videoFrameFolderRGB = "../" + folder + "/frames_rgb_test/" + title;

		// opens the audio inputStream
		FileInputStream audioStream;
		try {
		    audioStream = new FileInputStream(audioName);
		} catch (FileNotFoundException e) {
		    e.printStackTrace();
		    return;
		}
		//Build the player layout
		setUpPlayerLayout();
		// get the frameList to display
		Summarization_main sm = new Summarization_main(videoFrameFolderRGB, audioName);
		frameList = sm.getFrameList();
		// plays the video and sound after the player layout is set up
		paused = new AtomicBoolean(false);
		audioThread = new PlayAudioFile(audioStream, paused, frameList);
		videoThread = new PlayVideoFile(paused, frame, outputImg, labelConstraints, videoFrameFolder, frameList);
		audioThread.start();
		videoThread.start();
		try {
	    	audioThread.join();
	    	videoThread.join();
	    } catch (InterruptedException e) {
	    	System.out.println("interruptedException");
	    }
	}
    
    public static void setUpPlayerLayout() {
    	
	    GridBagLayout gLayout = new GridBagLayout();
	    frame = new JFrame("test");
	    frame.getContentPane().setLayout(gLayout);
	    button = new JButton("Start/Pause");
	    button.addActionListener(new ButtonListener());
	    labelConstraints = new GridBagConstraints();
		labelConstraints.fill = GridBagConstraints.HORIZONTAL;
		labelConstraints.anchor = GridBagConstraints.CENTER;
		labelConstraints.weightx = 0.5;
		labelConstraints.gridx = 0;
		labelConstraints.gridy = 0;
		buttonConstraints = new GridBagConstraints();
		buttonConstraints.fill = GridBagConstraints.HORIZONTAL;
		buttonConstraints.anchor = GridBagConstraints.BASELINE;
		buttonConstraints.weightx = 0.5;
		buttonConstraints.gridx = 0;
		buttonConstraints.gridy = 180;
		outputImg = new JLabel(); //label for displaying the continuous images
		panel = new JPanel(); //panel for holding the button
		panel.add(button);
		panel.setSize(width, 15);
		frame.getContentPane().add(panel, labelConstraints);
		frame.getContentPane().add(panel, buttonConstraints);	
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		frame.pack();
		frame.setVisible(true);
		
    }
    
    static class ButtonListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent evt) {
        	//if paused
            if(!paused.get()){
                paused.set(true);
            }
            //if running
            else{
            	paused.set(false);
                synchronized(videoThread){
                    videoThread.notifyAll();
                }
                synchronized(audioThread){
                    audioThread.notifyAll();
                }
            }
        }
    }
}

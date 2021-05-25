//package org.wikijava.sound.playWave;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.DataLine.Info;

/**
 * 
 * <Replace this with a short description of the class.>
 * 
 * @author Giulio
 */
public class PlayAudioFile extends Thread{

    private InputStream waveStream;

    private final int EXTERNAL_BUFFER_SIZE = 6400; // 128Kb
    AtomicBoolean paused;
    ArrayList<Integer> frameList;
    /**
     * CONSTRUCTOR
     */
    public PlayAudioFile(InputStream waveStream, AtomicBoolean paused, ArrayList<Integer> frameList) {
    	this.waveStream = waveStream;
    	this.paused = paused;
    	this.frameList = frameList;
    }

    public void run() {	
    	AudioInputStream audioInputStream = null;
    	InputStream bufferedIn = new BufferedInputStream(this.waveStream);
    	try {
			audioInputStream = AudioSystem.getAudioInputStream(bufferedIn);
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	int videoFrameThis = 0;
    	int videoFramePrev = 0;
	    //audioInputStream = AudioSystem.getAudioInputStream(this.waveStream);	
		//add buffer for mark/reset support, modified by Jian
    	//for(int i = 0; i < frameList.size(); i++) {	 	
			// Obtain the information about the AudioInputStream
			AudioFormat audioFormat = audioInputStream.getFormat();
			Info info = new Info(SourceDataLine.class, audioFormat);
			// opens the audio channel
			SourceDataLine dataLine = null;
			try {
				dataLine = (SourceDataLine) AudioSystem.getLine(info);
			} catch (LineUnavailableException e2) {
				e2.printStackTrace();
			}
		    try {
				dataLine.open(audioFormat);//, this.EXTERNAL_BUFFER_SIZE);
			} catch (LineUnavailableException e) {
				e.printStackTrace();
			}
			// Starts the music :P
			dataLine.start();		
			try {
				byte[] audioBuffer = new byte[this.EXTERNAL_BUFFER_SIZE];
				int readBytes = 0;
				int previous = this.frameList.get(0);
				
				System.out.println("skip bytes goal: " + previous*4*1600);
				System.out.println("skip bytes actual: " + audioInputStream.skip(previous*4*1600));

			    for(Integer i : this.frameList) {
		    		//long skipBytes = 0;
			    	if(paused.get()){
						synchronized(this){
							// Pause
							try{
								//System.out.println("paused!!");
								this.wait();
								//dataLine.wait();
							} 
							catch (InterruptedException e) {
								System.out.println("error happened");
							}
						}
					}			
			    	if(i - previous > 1) {
			    		long skipBytes = ((i - previous - 1)*4*1600);//skipframe*4*1600
						System.out.println("skip bytes goal: " + skipBytes);
			    		System.out.println("skip bytes actual: " + audioInputStream.skip(skipBytes));
			    	}
			    	// System.out.println("available bytes: " + audioInputStream.available());
					readBytes = audioInputStream.read(audioBuffer, 0,
						audioBuffer.length);	
					if (readBytes >= 0){
					    dataLine.write(audioBuffer, 0, readBytes);  
					}
					previous = i;	    
			    }
			    
		} 
		catch (IOException e1) 
		{
		    e1.printStackTrace();
		}
		finally 
		{
		    // plays what's left and and closes the audioChannel
		    dataLine.drain();
		    dataLine.close();
		}
	}
}

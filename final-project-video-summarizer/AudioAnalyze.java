
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.lang.Math;


// @auther Tianyi An


public class AudioAnalyze
{
    private int sampleSize;
    private int sampleRate;

	private int bytesPerFrame;
	private int frameRate;

    private long total_Frame_num;
    private int channel_num;

	private boolean signed;
	private boolean BigEndian;

	private int header_offset = 44;

	private byte[] data;

	String filedir = "./dataset/audio/concert.wav";

	public AudioAnalyze(String filename)
	{
		this.filedir = filename;
	}

	public void read_audio()
	{
		int totalFramesRead = 0;
		File fileIn = new File(this.filedir);
		// somePathName is a pre-existing string whose value was
		// based on a user selection.
		try {

			//get info
			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(fileIn);

			//number of samples take for 1 channel
			this.sampleRate = (int) audioInputStream.getFormat().getSampleRate();

			this.sampleSize = audioInputStream.getFormat().getSampleSizeInBits() / 8;

			this.frameRate = (int) audioInputStream.getFormat().getFrameRate();

			this.bytesPerFrame = audioInputStream.getFormat().getFrameSize();

			//Frame here = 1 sample from each channel
			this.total_Frame_num = audioInputStream.getFrameLength();

			this.channel_num = audioInputStream.getFormat().getChannels();

			this.BigEndian = audioInputStream.getFormat().isBigEndian();

			
			// System.out.println("Sample rate per sec (for 1 channel): " + this.sampleRate);
			// System.out.println("Frame rate per sec (1 frame = 2 samples): " + this.frameRate);
			// System.out.println("Size of each sample in bytes: " + this.sampleSize);
			// System.out.println("Size of each Frame in bytes: " + this.bytesPerFrame);
			// System.out.println("Channel number: " + this.channel_num);
			// System.out.println("Total Frames: " + this.total_Frame_num);
			// System.out.println("Is Big Endian: " + this.BigEndian);


			// Set an arbitrary buffer size of X frames.
			int numBytes = (int)total_Frame_num * bytesPerFrame;
			this.data = new byte[numBytes];

			try 
			{
					int numBytesRead = 0;
					int numFramesRead = 0;

					// Try to read numBytes bytes from the file.
					//AudioSystem.getAudioInputStream automatically discard header and only read the data part of wav file
					while ((numBytesRead = audioInputStream.read(this.data)) != -1) 
					{
						// Calculate the number of frames actually read.
						numFramesRead = numBytesRead / bytesPerFrame;
						totalFramesRead += numFramesRead;
						System.out.println("Total frames read: " + totalFramesRead);
						// Here, do something useful with the audio data that's 
						// now in the audioBytes array...
					}
			} 
			catch (Exception ex) 
			{ 
				// Handle the error...
			}
		} 
		catch (Exception e) 
		{
		// Handle the error...
		}
	}

 	/**
     * Returns sample (amplitude value). Note that in case of stereo samples
     * go one after another. I.e. 0 - first sample of left channel, 1 - first
     * sample of the right channel, 2 - second sample of the left channel, 3 -
     * second sample of the rigth channel, etc.
	 * Number of channels (1 for mono, 2 for stereo, etc.)
     */
	public double get_1_frame_amplitude(int ind)
	{
		if (ind < 0 || ind >= this.total_Frame_num) 
		{
			throw new IllegalArgumentException(
					"sample number can't be < 0 or >= "+ this.total_Frame_num
					);
		}

		double amplitude = 0;
		byte[] single_sample1 = new byte[this.sampleSize];
		byte[] single_sample2 = new byte[this.sampleSize];
		for(int i=0; i < this.sampleSize; i++)
		{
			single_sample1[i] = this.data[ind + i];
			single_sample2[i] = this.data[ind + i + this.sampleSize];
		}

		double left = Math.abs( (double)((single_sample1 [0] & 0xff) | (single_sample1[1] << 8)) );
		double right = Math.abs( (double)((single_sample2 [0] & 0xff) | (single_sample2[1] << 8)) );
		amplitude = (left+right)/2.0;
		amplitude = amplitude/32768;

		// System.out.println("original: " + Arrays.copyOfRange(this.data, ind, ind + this.bytesPerFrame));
		// System.out.println("left value: " + left);
		// System.out.println("right value: " + right);
		// System.out.println("amplitude: " + amplitude);
		return amplitude;
	}

	public double get_score_of_shot(int start_video_frame, int end_video_frame)
	{
		if (start_video_frame < 0 || start_video_frame > 16200) 
		{
			throw new IllegalArgumentException(
					"start_video_frame has to be in [0, 16200]"
					);
		}
		if (end_video_frame < 0 || end_video_frame > 16200) 
		{
			throw new IllegalArgumentException(
					"end_video_frame has to be in [0, 16200]"
					);
		}
		double total_amplitude = 0;
		double video_frame_rate = 30.0;

		int start_audio_frame = (int)( (double)start_video_frame/video_frame_rate*this.frameRate );
		int end_audio_frame = (int)( (double)end_video_frame/video_frame_rate*this.frameRate );

		for(int i=start_audio_frame; i<end_audio_frame; i++)
		{
			total_amplitude += get_1_frame_amplitude(i);
		}

		double audio_score = total_amplitude/(end_audio_frame-start_audio_frame);
		
		// System.out.println("Score of video shot frame " + start_video_frame + " to " + end_video_frame);
		// System.out.println("is computed from audio frame " + start_audio_frame + " to " + end_audio_frame);
		// System.out.println("score of shot: " + audio_score + " ( range: [0,1] )");

		return audio_score;
	}

	// public static void main(String[] args) 
	// {
	// 	String filedir = args[0];
	// 	System.out.println(filedir);
	// 	AudioAnalyze audio_obj = new AudioAnalyze(filedir);
	// 	audio_obj.read_audio();
	// 	// audio_obj.get_score_of_shot(0, 16199);
	// 	audio_obj.get_score_of_shot(25, 30);
	// }

}

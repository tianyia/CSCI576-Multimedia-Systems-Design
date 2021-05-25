import java.io.File;
import java.util.*;

public class Summarization_main
{
    String filename;

	//with default example
    String videofiledir = "./dataset/frames_rgb/concert";
	String audiofiledir = "./dataset/audio/concert.wav";
	static ArrayList<Integer> frameList;

    Summarization_main(String videoroute, String audioroute)
    {
		this.videofiledir = videoroute;
        this.audiofiledir = audioroute;
    }
    

	public ArrayList<Integer> getFrameList() 
	{
		//String filename = args[0];
		//String filename = "concert";

      		File folder = new File(this.videofiledir);
      	    List<File> fileList = Arrays.asList(folder.listFiles());
      		
      	    //get sorted list of frames from folder path
      		FrameOperations op = new FrameOperations();
      		MotionVector mv = new MotionVector();
      		HistogramScore hs = new HistogramScore();
      		List<File> list = op.getFrameList(fileList);
      		
      		//find the list of key frames for shots
      		ArrayList<Integer> keyFrames = mv.findKeyFrames(list);
      		System.out.println("number of key frames is: " + keyFrames.size());
      		for(int i = 0; i < keyFrames.size(); i++) {
      			System.out.print(keyFrames.get(i) + ", ");
      		}


      		//audio analyzer initialization
      		AudioAnalyze audio_obj = new AudioAnalyze(this.audiofiledir);
      		audio_obj.read_audio();

      		//find shots with highest score until total frame number exceed 2700 (30s)
              System.out.println("");
              HashMap<Shot, Double> shots = new HashMap<Shot, Double>();

              // Compute score and enter data into hashmap
              double score = 0;

              ArrayList<Integer> new_keyFrames = new ArrayList<Integer>();
              int pre_i = 0;
              new_keyFrames.add(keyFrames.get(0));
              for(int i = 0; i < keyFrames.size(); i++)
              {
                  if(i > 0 && keyFrames.get(i) - keyFrames.get(pre_i) >= 30)
                  {
                    new_keyFrames.add(keyFrames.get(i));
                    pre_i = i;
                  }
              }

              for(int i = 0; i < new_keyFrames.size(); i++)
              {
                  if(i == new_keyFrames.size()-1)
                  {
                      if( new_keyFrames.get(i) >= 16199 )
                      {
                          break;
                      }
                      else
                      {
                          //compute score
                          score = hs.findShotScoreHist(new_keyFrames.get(i), 16200-1, list)/16/(16200-1-new_keyFrames.get(i))*0.8 +
                                  mv.findShotScoreMV(new_keyFrames.get(i), 16200-1, list)/16/(16200-1-new_keyFrames.get(i)) +
                                  audio_obj.get_score_of_shot(new_keyFrames.get(i), 16200)/2;
                                  
                          shots.put(new Shot(new_keyFrames.get(i), 16200), score);
                      }
                  }
                  else
                  {
                      //compute score
                      score = hs.findShotScoreHist(new_keyFrames.get(i), new_keyFrames.get(i+1), list)/16/(new_keyFrames.get(i+1)-new_keyFrames.get(i))*0.8 +
                              mv.findShotScoreMV(new_keyFrames.get(i), new_keyFrames.get(i+1), list)/16/(new_keyFrames.get(i+1)-new_keyFrames.get(i)) +
                              audio_obj.get_score_of_shot(new_keyFrames.get(i), new_keyFrames.get(i+1))/2;

                      shots.put(new Shot(new_keyFrames.get(i), new_keyFrames.get(i+1)), score);
                  }
              }

              // sort hashmap and generate arraylist of frames to be played
              Map<Shot, Double> sorted_shots = sortByValue(shots);
              ArrayList<Integer> selected_frames = new ArrayList<Integer>();
              int selected_frames_num = 0;
              for (Map.Entry<Shot, Double> en : sorted_shots.entrySet()) 
              {
                  System.out.println("shot start = " + en.getKey().get_start() +
      							", shot end = " +  en.getKey().get_end() +
                                ", Score = " + en.getValue());
                  
                  for(int j = en.getKey().get_start(); j < en.getKey().get_end(); j++)
                  {
                      selected_frames.add(j);
                      selected_frames_num += 1;
                      if(selected_frames_num >= 3000) //reach maximum length
                      {
                          break;
                      }
                  }
                  // selected_frames_num += en.getKey().get_end() - en.getKey().get_start();
                  if(selected_frames_num >= 2700) //more than required length
                  {
                      break;
                  }
              }
              
              Collections.sort(selected_frames);
              System.out.println("selected frames:" + selected_frames.size());
      		for(int i = 0; i < selected_frames.size(); i++) {
      			System.out.print(selected_frames.get(i) + ", ");
      		}
              System.out.println("");
              System.out.println("selected frame number:" + selected_frames.size());
              System.out.println("time length:" + selected_frames.size()/30.0 + " sec");
			return selected_frames;

      	}

          // function to sort hashmap by values
          public static HashMap<Shot, Double> sortByValue(HashMap<Shot, Double> hm)
          {
              // Create a list from elements of HashMap
              List<Map.Entry<Shot, Double> > list =
                     new LinkedList<Map.Entry<Shot, Double> >(hm.entrySet());
       
              // Sort the list
              Collections.sort(list, new Comparator<Map.Entry<Shot, Double> >() {
                  public int compare(Map.Entry<Shot, Double> o1,
                                     Map.Entry<Shot, Double> o2)
                  {
                      return (o2.getValue()).compareTo(o1.getValue());
                  }
              });
               
              // put data from sorted list to hashmap
              HashMap<Shot, Double> temp = new LinkedHashMap<Shot, Double>();
              for (Map.Entry<Shot, Double> aa : list) {
                  temp.put(aa.getKey(), aa.getValue());
              }
              return temp;
          }

      }


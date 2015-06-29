package edu.dal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConsolidateResults {
	
	
	public static void main (String [] args){
		//evaluateLeaveOneEpinionsData();
		StringBuffer buffer = new StringBuffer();
		for (int totalRatings=1;totalRatings<=5;totalRatings++){
			BufferedReader reader = null;
			String line = null;
			
			try{
				reader = new BufferedReader(new FileReader("/Users/dalthuru/Documents/Trust/Implementation/Sequential/src/TrustRatings_epinions_ext_cs_"+totalRatings+"_d2.txt"));
				/*if (totalRatings == 1)
					reader = new BufferedReader(new FileReader("/Users/dalthuru/Documents/Trust/Implementation/Sequential/src/TrustRatings_epinions_ext_cs_619.txt"));
				else 
					reader = new BufferedReader(new FileReader("/Users/dalthuru/Documents/Trust/Implementation/Sequential/src/TrustRatings_epinions_cs_ext_cons.csv"));*/
				//reader = new BufferedReader(new FileReader("/Users/dalthuru/Documents/Trust/Implementation/Sequential/src/TrustRatings_epinions_cs_"+totalRatings+".txt"));
				while ((line = reader.readLine()) != null){
					if (null != line && line.trim().length()>0){
						/*String[]  splits = line.split(",");
						String tidalRating = splits[3];
						String partialRating = splits[4];
						if (null != tidalRating && (new Float (tidalRating)).floatValue() >0 &&  !tidalRating.equals(partialRating)){
							buffer.append(line);
							buffer.append("\n");
						}*/	
						buffer.append(line);
						buffer.append("\n");
					}
					
				}
				
			} catch (Exception e){
				e.printStackTrace();
			} finally {
				if (reader != null){
					try{
						reader.close();
					} catch (IOException io){
						
					}
				}
			}
			
			
		}
		writeToFile(buffer);
	}
	
	
	private static void writeToFile (StringBuffer buffer){
		BufferedWriter bufferWritter = null;
		try{
    		
 
    		File file =new File("TrustRatings_epinions_cs_ext_1_5_d2_cons.csv");
 
    		//if file doesnt exists, then create it
    		if(!file.exists()){
    			file.createNewFile();
    		}
 
    		//true = append file
    		bufferWritter = new BufferedWriter(new FileWriter(file.getName(),true));
    	   
    	    bufferWritter.write(buffer.toString());
    	    bufferWritter.close();
 
	        System.out.println("Done");
 
    	}catch(IOException e){
    		e.printStackTrace();
    	} finally {
    		try{
	    		if (bufferWritter != null){
	    			bufferWritter.close();
	    		}
    		}catch(IOException e){
        		e.printStackTrace();
        	} 
        		
    	}
	}

}

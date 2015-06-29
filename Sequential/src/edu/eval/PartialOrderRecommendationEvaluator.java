package edu.eval;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.dal.DBConnectionManager;
import edu.loader.DataLoader;
import edu.loader.EpinionsDataLoader;
import edu.loader.ExtendedEpinionsDataLoader;
import edu.models.RecommenderResult;
import edu.models.UserBo;
import edu.models.UserItemRating;
import edu.models.RecommenderResult.AlgorithmType;
import edu.recommender.ExtendedModPartialOrderTrustRecommender;
import edu.recommender.ExtendedTidalTrustRecommender;
import edu.recommender.PartialOrderTrustRecommender;
import edu.recommender.RecommendUserItemData;
import edu.recommender.ResnicRecommender;
import edu.recommender.TidalTrustRecommender;
import edu.trust.RecommendationResult;

public class PartialOrderRecommendationEvaluator {
	
	public static void evaluateLeaveOneEpinionsData(){
		DataLoader loader = EpinionsDataLoader.getInstance();
		List<UserItemRating> userItemList = loader.getUserItemRatings();
		int count = 0;
		int rCount = 0;
		long start = System.currentTimeMillis();
		StringBuffer buffer = new StringBuffer();
		int i=1;
		int size = userItemList.size();
		System.out.println ("Ti"+size);
		for (int j=0;j<userItemList.size();j++){
			UserItemRating userItemRating = userItemList.get(j);
		
			LeaveOneUserItemData excludedData = new LeaveOneUserItemData();
			excludedData.setItemId(userItemRating.getItemId());
			excludedData.setUserId(userItemRating.getUserId());
			excludedData.setLoader(loader);
			DataLoader leaveOneLoader = new EpinionsLeaveOneDataManager(excludedData);
			RecommendUserItemData recommendData = new RecommendUserItemData();
			recommendData.setItemId(userItemRating.getItemId());
			recommendData.setUserId(userItemRating.getUserId());
			recommendData.setLoader(leaveOneLoader);
			Float prediction2=-2F;
			Float prediction3=-2F;
			Float prediction4=-2F;
			
			Float poPrediction2=-2F;
			
			
			
			Float prediction1 = TidalTrustRecommender.getUserItemRecommendation(recommendData,1);
			if (prediction1 == null || prediction1 <=0F){
				prediction2 = TidalTrustRecommender.getUserItemRecommendation(recommendData,2);
			}	
			
			RecommenderResult result  = PartialOrderTrustRecommender.getUserItemRecommendation(recommendData,1);
			Float poPrediction1 = result.itemRating;
			if (poPrediction1 == null || poPrediction1 <=0F){
				result = PartialOrderTrustRecommender.getUserItemRecommendation(recommendData,2);
				poPrediction2 =  result.itemRating;
			}	
			
			Float rPrediction = ResnicRecommender.getUserItemRecommendation(recommendData);
			buffer.append(userItemRating.getUserId()+","+userItemRating.getItemId()+","+userItemRating.getRating()+","+rPrediction+","+prediction1+","+prediction2+","+prediction3+","+prediction4+","+poPrediction1+","+poPrediction2);
			System.out.println (userItemRating.getUserId()+","+userItemRating.getItemId()+","+userItemRating.getRating()+","+rPrediction+","+prediction1+","+prediction2+","+prediction3+","+prediction4+","+poPrediction1+","+poPrediction2);
			buffer.append("\n");
			count++;
			if (count == 50){
				rCount++;
				//writeToFile(buffer.toString(), i);
				buffer = new StringBuffer();
				count=0;
			}
			if (rCount == 10){
				i++;
			}
		}
		long end = System.currentTimeMillis();
		System.out.println ("TidalCount:"+count+";ResnicCount:"+rCount+":time ="+(end-start));
		
		
		
	}
	
	
	/**
	 * For standard epinions data set. Takes the userIds and the number of rating provided by the user
	 * and outputs the result
	 * @param userIds
	 * @param totalRatings
	 */
	public static void evaluateLeaveOneEpinionsData(List<Long> userIds, int totalRatings, int depth){
		DataLoader loader = EpinionsDataLoader.getInstance();
		StringBuffer buffer = new StringBuffer();
		long start = System.currentTimeMillis();
		int count = 0;
		
		for (Long userId : userIds){
			UserBo userBo = loader.getUserBoMap().get(userId);
			Map<Long, Float> itemRatings = userBo.getItemRatingsMap();
			Set<Long> itemIds = itemRatings.keySet();			
			for (Long itemId : itemIds){				
				LeaveOneUserItemData excludedData = new LeaveOneUserItemData();
				excludedData.setItemId(itemId);
				excludedData.setUserId(userId);
				excludedData.setLoader(loader);
				DataLoader leaveOneLoader = new EpinionsLeaveOneDataManager(excludedData);
				RecommendUserItemData recommendData = new RecommendUserItemData();
				recommendData.setItemId(itemId);
				recommendData.setUserId(userId);
				recommendData.setLoader(leaveOneLoader);
				Float prediction1 = TidalTrustRecommender.getUserItemRecommendation(recommendData,1);
				
				RecommenderResult result  = PartialOrderTrustRecommender.getUserItemRecommendation(recommendData,1);
				Float poPrediction1 = result.itemRating;
				
				Float rPrediction = ResnicRecommender.getUserItemRecommendation(recommendData);
				
				buffer.append(userId+","+itemId+","+itemRatings.get(itemId)+","+rPrediction+","+prediction1+","+poPrediction1);
				//System.out.println (userId+","+itemId+","+itemRatings.get(itemId)+","+rPrediction+","+prediction1+","+poPrediction1);
				buffer.append("\n");
				count++;
				if (count == 100){					
					writeToFile(buffer.toString(), totalRatings, depth);
					buffer = new StringBuffer();
					count=0;
				}
				
			}
		}	
		long end = System.currentTimeMillis();
		System.out.println ("time ="+(end-start));
		
		
		
	}
	
	
	/**
	 * For standard epinions data set. Takes the userIds and the number of rating provided by the user
	 * and outputs the result
	 * @param userIds
	 * @param totalRatings
	 */
	public static void evaluateStandardLeaveOneEpinionsData(List<UserItemRating> uIRatings, int totalRatings, int depth){
		DataLoader loader = EpinionsDataLoader.getInstance();
		StringBuffer buffer = new StringBuffer();
		long start = System.currentTimeMillis();
		
		for (UserItemRating uIRating : uIRatings){
		
			Long itemId = uIRating.getItemId();
			Long userId = uIRating.getUserId();
			Float rating = uIRating.getRating();
			
			LeaveOneUserItemData excludedData = new LeaveOneUserItemData();
			excludedData.setItemId(itemId);
			excludedData.setUserId(userId);
			excludedData.setLoader(loader);
			DataLoader leaveOneLoader = new EpinionsLeaveOneDataManager(excludedData);
			RecommendUserItemData recommendData = new RecommendUserItemData();
			recommendData.setItemId(itemId);
			recommendData.setUserId(userId);
			recommendData.setLoader(leaveOneLoader);
			Float prediction1 = TidalTrustRecommender.getUserItemRecommendation(recommendData,1);			
			RecommenderResult result  = PartialOrderTrustRecommender.getUserItemRecommendation(recommendData,1);
			Float poPrediction1 = result.itemRating;			
			Float rPrediction = ResnicRecommender.getUserItemRecommendation(recommendData);			
			buffer.append(userId+","+itemId+","+rating+","+rPrediction+","+prediction1+","+poPrediction1);
			//System.out.println(userId+","+itemId+","+rating+","+rPrediction+","+prediction1+","+poPrediction1);			
			buffer.append("\n");
		}	
		writeToFile(buffer.toString(), totalRatings, depth);
		long end = System.currentTimeMillis();
		System.out.println ("time ="+(end-start));
		
		
		
	}
	
	/**
	 * Evaluator for extended epinions data set
	 * @param userIds
	 * @param totalRatings
	 */
	public static void evaluateExtendedLeaveOneEpinionsData(List<Long> userIds, int totalRatings, int depth){
		//DataLoader loader = EpinionsDataLoader.getInstance();
		DataLoader loader = ExtendedEpinionsDataLoader.getInstance();
		StringBuffer buffer = new StringBuffer();
		long start = System.currentTimeMillis();
		System.out.println ("UserId, ItemId, ActualRating, PredictedRating, time, DB_hits");
		//int count = 0;
		for (Long userId : userIds){
			List<UserItemRating> userItemRatings = DBConnectionManager.getUserItemRatings(userId);
			
			StringBuffer interBuffer = getExtendedPredictions(userItemRatings, loader, depth);
			buffer.append(interBuffer.toString());
			/*count++;
			if (count == 100){
				
				writeToFile(buffer.toString(), totalRatings);
				buffer = new StringBuffer();
				count=0;
			}*/
		}	
		writeToFile(buffer.toString(), totalRatings, depth);
		long end = System.currentTimeMillis();
		System.out.println ("time ="+(end-start));
	}
	
	
	public static void evaluateExtendedLeaveOneEpinionsData(List<UserItemRating> userItemRatings, int depth){
		long start = System.currentTimeMillis();
		DataLoader loader = ExtendedEpinionsDataLoader.getInstance();
		StringBuffer buffer = getExtendedPredictions(userItemRatings, loader, depth);
		writeToFile(buffer.toString(), 20, depth);
		long end = System.currentTimeMillis();
		System.out.println ("time ="+(end-start));
	}
	
	
	private static StringBuffer getExtendedPredictions(List<UserItemRating> userItemRatings, DataLoader loader, int depth){
		StringBuffer buffer = new StringBuffer();
		
		for (UserItemRating userItemRating : userItemRatings){
			Long itemId = userItemRating.getItemId();
			Float actualRating = userItemRating.getRating();
			Long userId = userItemRating.getUserId();
			//DBConnectionManager.deleteUserItemRating(userItemRating);
			RecommendUserItemData recommendData = new RecommendUserItemData();
			recommendData.setItemId(itemId);
			recommendData.setUserId(userItemRating.getUserId());
			recommendData.setLoader(loader);
			
			long prevHits = DBConnectionManager.hits;
			
			try{
				//Float prediction1 = 0F;
				long start = System.currentTimeMillis();
				
				RecommendationResult prediction1 = ExtendedTidalTrustRecommender.getUserItemRecommendation(recommendData,depth);
				long end = System.currentTimeMillis();
				System.out.println (userId+", "+itemId+", "+actualRating+", "+prediction1.getPrediction()+", "+ (end-start)+","+(DBConnectionManager.hits-prevHits));				
				buffer.append(userId+","+itemId+","+actualRating+","+prediction1);
			} catch (Throwable t){
				t.printStackTrace();
				
				System.out.println("Exception in getting recommendations for userId:"+
						userItemRating.getUserId()+":ItemId"+userItemRating.getItemId()+";rating"+
						userItemRating.getRating());
			}
			//DBConnectionManager.insertUserItemRating(userItemRating);
			buffer.append("\n");
		}
		return buffer;
		
	}
	
	
	
	
	private static void writeToFile (String str, Integer split, int depth){
		BufferedWriter bufferWritter = null;
		try{
    		
    		File file =new File("TrustRatings_epinions_ext_cs_2014"+split+"_d"+depth+".txt");
			//File file =new File("TrustRatings_epinions_ext_contro_items_d2.txt");
 
    		//if file doesnt exists, then create it
    		if(!file.exists()){
    			file.createNewFile();
    		} 
    		//true = append file
    		bufferWritter = new BufferedWriter(new FileWriter(file.getName(),true));
    		bufferWritter.write(str);
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
	
	
	private static List<UserItemRating> loadControverialUserItemRatings(String fileName){
		BufferedReader reader = null;
		String line = null;
		List<UserItemRating> uIRatings = new ArrayList<UserItemRating>();
		try{
			reader = new BufferedReader(new FileReader(fileName));
		
			while ((line = reader.readLine()) != null){
				String[] splits = line.split(",");
				if (null != splits){
					
					UserItemRating uIRating = new UserItemRating();
					uIRating.setUserId(new Long(splits[0]));
					uIRating.setItemId(new Long(splits[1]));
					uIRating.setRating(new Float(splits[2]));
					uIRatings.add(uIRating);
					
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
		return uIRatings;
	}
	
	public static void main (String [] args){
		//evaluateLeaveOneEpinionsData();
		/*for (int totalRatings=1;totalRatings<=1;totalRatings++){
			BufferedReader reader = null;
			String line = null;
			List<Long> userIds = new ArrayList<Long>();
			try{
				//reader = new BufferedReader(new FileReader("/Users/dalthuru/Documents/Trust/Implementation/epinions/TrustAndDistrust/coldstart/users_"+totalRatings+"itemrating.csv"));
				reader = new BufferedReader(new FileReader("/Users/dalthuru/Documents/Trust/Implementation/epinions/TrustAndDistrust/coldstart/users_6_19_itemrating.csv"));
				//reader = new BufferedReader(new FileReader("/Users/dalthuru/Documents/Trust/Implementation/epinions/TrustAndDistrust/coldstart/users_"+totalRatings+"itemrating.csv"));
				//reader = new BufferedReader(new FileReader("Controverial_user_items_extended.csv"));
			
				while ((line = reader.readLine()) != null){
					String[] splits = line.split(",");
					if (null != splits){
						
						userIds.add(new Long( splits[0]));
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
			
			evaluateExtendedLeaveOneEpinionsData(userIds,619);
			//evaluateExtendedLeaveOneEpinionsData(userIds, totalRatings);
		}	*/
		
		//List<UserItemRating> ratings = loadControverialUserItemRatings("Opiniated_user_items_ext.csv");
		//evaluateExtendedLeaveOneEpinionsData(ratings, 3);
		for (int totalRatings=1;totalRatings<=1;totalRatings++){
			BufferedReader reader = null;
			String line = null;
			List<Long> userIds = new ArrayList<Long>();
			try{
				//reader = new BufferedReader(new FileReader("/Users/dalthuru/Documents/Trust/Implementation/epinions/TrustAndDistrust/coldstart/users_"+totalRatings+"itemrating.csv"));
				//reader = new BufferedReader(new FileReader("/Users/dalthuru/Documents/Trust/Implementation/epinions/TrustAndDistrust/coldstart/users_6_19_itemrating.csv"));
				reader = new BufferedReader(new FileReader("/Users/dalthuru/Documents/Trust/Implementation/epinions/TrustAndDistrust/coldstart/users_"+totalRatings+"itemrating.csv"));
				//reader = new BufferedReader(new FileReader("Controverial_user_items_extended.csv"));
			
				while ((line = reader.readLine()) != null){
					String[] splits = line.split(",");
					if (null != splits){
						
						userIds.add(new Long( splits[0]));
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
			
			//evaluateExtendedLeaveOneEpinionsData(userIds,619);
			evaluateExtendedLeaveOneEpinionsData(userIds, totalRatings,1);
			//evaluateExtendedLeaveOneEpinionsData(userIds, totalRatings,2);
			System.out.println("Hit number is"+DBConnectionManager.hits);
		}
		
	}
}

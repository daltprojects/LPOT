package edu.similarity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.loader.DataLoader;
import edu.models.UserItemRating;

/**
 * 
 * Computes similarity ratings based on Pearson coefficient.
 *
 */
public class Similarity {
	
	
	
	private static Similarity similarity = null;
	
	private Similarity(){};
	
	public static Similarity getInstance(){
		if (null == similarity){
			similarity = new Similarity();
			
		}
		
		return similarity;
	}
	
	public  Map<Long,UserSimilarityData> getSimilarityMatrix(DataLoader loader){
		
		Map<Long,UserSimilarityData> retVal = new HashMap<Long,UserSimilarityData>();
		Set<Long> userIds = loader.getUsers();	
		
		for (long userId : userIds){
			
			
			retVal.put(userId,getSimilarityMatrix(loader,userId));
		}
		return retVal;
	}
	
	public UserSimilarityData getSimilarityMatrix(DataLoader loader, Long userId){
		
		Set<Long> userIds = loader.getUsers();	
		UserSimilarityData similarityData = new UserSimilarityData(userId);
		
			
		for (long simUserId : userIds){
			if (simUserId != userId){
				float pearsonCoeff = getPearsonCoeff(userId, simUserId, loader);
				similarityData.setSimilarity(simUserId, pearsonCoeff);
			}
		}
		return similarityData;
	}
	
	private Float getPearsonCoeff(Long user1, Long user2, DataLoader loader){
		
		if (null == user1 || null == user2){
			return 0.0f;
		}
		List<Long> user1Items = loader.getUserItemHash().get(user1);
		List<Long> user2Items = loader.getUserItemHash().get(user2);
		List<Long> matchedItems = new ArrayList<Long>();
		for (Long user1Item : user1Items){
			if (user2Items.contains(user1Item)){
				matchedItems.add(user1Item);
			}
		}
		if (matchedItems.size()==0){
			return 0.0f;
		}
		int matchLen = matchedItems.size();
		double sum1=0.0f;
		double sum2=0.0f;
		double sum1Sq=0.0f;
		double sum2Sq=0.0f;
		double sumProd=0.0f;
		for (Long matchedItem : matchedItems){
			sum1+= getRating(user1, matchedItem, loader);
			sum2+= getRating(user2, matchedItem, loader);
			sum1Sq+= Math.pow(getRating(user1, matchedItem, loader),2);
			sum2Sq+= Math.pow(getRating(user1, matchedItem, loader),2);
			sumProd+=getRating(user1, matchedItem, loader)*getRating(user2, matchedItem, loader);
		}
		double numer = sumProd-(sum1*sum2/matchLen);
		double denom = Math.sqrt((sum1Sq-(Math.pow(sum1,2))/matchLen)*(sum2Sq-(Math.pow(sum2,2))/matchLen));
		if(denom==0){
			return 0.0f;
		} else {
			return (float)(numer/denom);
		}
		

		
	}
	
	private float getRating(Long userId, Long itemId,DataLoader loader){
		
		List<UserItemRating> userRatings = loader.getUserItemRatingsHash().get(userId);
		for (UserItemRating userRating : userRatings){
			if (userRating.getItemId()==itemId.longValue()){
				return userRating.getRating();
			}
			
		}
		return 0.0f;
	}
	
	public static void main (String[] args){
		Similarity sim = Similarity.getInstance();
	}
	
}

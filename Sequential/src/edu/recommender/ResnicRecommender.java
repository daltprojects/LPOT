package edu.recommender;

import java.util.List;

import edu.loader.DataLoader;
import edu.models.UserBo;
import edu.models.UserItemRating;
import edu.similarity.Similarity;
import edu.similarity.UserSimilarityData;

public class ResnicRecommender {
	
	
	/**
	 * Get ItemId and userId for which rating need to be provided
	 * Get users that already rated the item
	 * 
	 * @param recommendData
	 * @return
	 */
	
	public static Float getUserItemRecommendation(RecommendUserItemData recommendData){
		
		Float prediction = 0.0F;
		Long itemId = recommendData.getItemId();
		Long userId = recommendData.getUserId();
		DataLoader data = recommendData.getLoader();
		
		
		 // Get Users that already rated this item
		 List<Long> users = data.getItemUserHash().get(itemId);
		 
		 if (users == null || users.isEmpty()){
			 return -1F;// No other user rated this item
		 }
		 
		 UserBo userBo = data.getUserBoMap().get(userId);
		 if (userBo.getItemsRated() == null || userBo.getItemsRated().isEmpty()){
			 return -11F;
		 }
		 
		// Get Similarity Matrix
			// Get User Similarity for current user
			UserSimilarityData similarityData = Similarity.getInstance().getSimilarityMatrix(recommendData.getLoader(), userId);
			
		 
		 // Loop through the user data
		 Float num = 0.0F;
		 Float den = 0.0F;
		 for (Long user: users){
			 
			 // Check if similartiy is greater than 0
			 Float similarity = similarityData.getSimilarity().get(user);
			 if (similarity != null && similarity >0){
				
				 Float avgRating = getUserAverageRating(data, user);
				 Float userRating = getUserRating(data, user, itemId);
				 
				 num += ((similarity)* (userRating - avgRating));
				 den += Math.abs(similarity);
				 
				 
			 }
		 }
		 if (den == 0F){
			 return -2F;// Users doesn't have ratings in common
		 }
		 prediction = num/den;
		 prediction += getUserAverageRating(data, userId);
		 return prediction;
		 
		 
	}
	
	
	private static Float getUserAverageRating(DataLoader data, Long user){
			
			Float average = 0.0F;
			List<UserItemRating> itemRatings =  data.getUserItemRatingsHash().get(user);
			
			if (null != itemRatings){
				int count = itemRatings.size();
				double sum =0;
				for (UserItemRating rating : itemRatings){
					
					sum+= rating.getRating();
				}
				average = new Float(sum/count);
			}
			return average;
	}
	
	private static Float getUserRating(DataLoader data, Long user, Long itemId){
		
		Float rating = 0.0F;
		List<UserItemRating> itemRatings =  data.getUserItemRatingsHash().get(user);
		
		if (null != itemRatings){
			for (UserItemRating uIRating : itemRatings){
				
				if (uIRating.getItemId() == itemId){
					return uIRating.getRating();
				}
			}
			
		}
		return rating;
}
	
}

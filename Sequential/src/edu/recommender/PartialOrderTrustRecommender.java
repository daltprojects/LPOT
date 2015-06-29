package edu.recommender;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import edu.loader.DataLoader;
import edu.models.RecommenderResult;
import edu.models.RecommenderResult.TrustResult;
import edu.trust.ExtendedPartialOrderTrustProcessor;
import edu.trust.RefPartialOrderTrustProcessor;
import edu.trust.UserTrustRequest;
import edu.trust.UserTrustResponse;

public class PartialOrderTrustRecommender {
	
public static RecommenderResult getUserItemRecommendation(RecommendUserItemData recommendData, int maxDepth){
		
	    RecommenderResult result = new RecommenderResult();
	    result.itemRating = 0.0F;
		Long itemId = recommendData.getItemId();
		Long userId = recommendData.getUserId();
		DataLoader data = recommendData.getLoader();
		
		UserTrustRequest req = new UserTrustRequest();
		req.setItemId(itemId);
		req.setUserId(userId);
		req.setLoader(data);
		req.setMaxDepth(maxDepth);
		
		//UserTrustResponse resp = getResp(req);
		UserTrustResponse resp = RefPartialOrderTrustProcessor.getTrustRatings(req);
		//Tets
		
		
		if (resp == null){
			result.trustResult = TrustResult.TIMED_OUT;
			result.itemRating = -21F;
			return result; // Couldn't finish in specified time-frame
		}
		 
		Map<Long, Float> userTrustMap = resp.getUserTrustRatingsMap();
		
		Float numer = 0.0F;
		Float denom = 0.0F;
		if (userTrustMap == null || userTrustMap.isEmpty()){
			result.trustResult = TrustResult.NO_INFO;
			result.itemRating = -22F;
			return result; 
		}
		
		int userTrustCount = 0;
		int userDistrustCount = 0;
		// Get the counts of users 
		for (Long tUserId: userTrustMap.keySet()){
			Float tRating = userTrustMap.get(tUserId);
			if (tRating > 0 ){
				userTrustCount++;
			} else {
				userDistrustCount++;
			}
		}
		if (userTrustCount == userDistrustCount){
			result.trustResult = TrustResult.AMBIGOUS;
			result.itemRating = -23F;
			return result; 
		} else if (userDistrustCount > userTrustCount){
			result.trustResult = TrustResult.NO_INFO;
			result.itemRating = -22F;
			return result; 
		}
		
		for (Long tUserId: userTrustMap.keySet()){
			Float iRating = data.getUserBoMap().get(tUserId).getItemRatingsMap().get(itemId);
			/*Float iRating = 0F;
			UserItemRating userItemRating = DBConnectionManager.getUserItemRatingByUserIdItemId(tUserId, itemId);
			if (userItemRating != null && userItemRating.getItemId()>0L){
				iRating = userItemRating.getRating();
			}*/
			Float tRating = userTrustMap.get(tUserId);
			if (iRating>0F && tRating>0){
				numer += tRating*iRating;
				denom += tRating;
			}
			
		}
		if (denom>0){
			result.itemRating = numer/denom;
		}
		
		 return result;
		 
		 
	}

 private static UserTrustResponse getResp (final UserTrustRequest req){
	 
	 UserTrustResponse resp = null;
	 ExecutorService executor = Executors.newCachedThreadPool();
		Callable<Object> task = new Callable<Object>() {
		   public Object call() {
			   return ExtendedPartialOrderTrustProcessor.getTrustRatings(req);
			   
		   }
		};
		Future<Object> future = executor.submit(task);
		try {
			resp = (UserTrustResponse)future.get(60, TimeUnit.SECONDS); 
		} catch (TimeoutException ex) {
		   // handle the timeout
		} catch (InterruptedException e) {
		   // handle the interrupts
		} catch (ExecutionException e) {
		   // handle other exceptions
		} finally {
		   future.cancel(true);// may or may not desire this
		}
		return resp;
	}
}

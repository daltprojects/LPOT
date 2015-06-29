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
import edu.trust.ExtendedTidalTrustProcessor;
import edu.trust.TidalTrustProcessor;
import edu.trust.UserTrustRequest;
import edu.trust.UserTrustResponse;

public class TidalTrustRecommender {
	
public static Float getUserItemRecommendation(RecommendUserItemData recommendData, int maxDepth){
		
		Float prediction = 0.0F;
		Long itemId = recommendData.getItemId();
		Long userId = recommendData.getUserId();
		DataLoader data = recommendData.getLoader();
		
		UserTrustRequest req = new UserTrustRequest();
		req.setItemId(itemId);
		req.setUserId(userId);
		req.setLoader(data);
		req.setMaxDepth(maxDepth);
		
		//UserTrustResponse resp = getResp(req);
		UserTrustResponse resp = TidalTrustProcessor.getTrustRatings(req);
		//Tets
		
		
		if (resp == null){
			return -3.0F;
		}
		 
		Map<Long, Float> userTrustMap = resp.getUserTrustRatingsMap();
		
		Float numer = 0.0F;
		Float denom = 0.0F;
		if (userTrustMap.isEmpty()){
			return -1F;
		}
		
		for (Long tUserId: userTrustMap.keySet()){
			Float iRating = data.getUserBoMap().get(tUserId).getItemRatingsMap().get(itemId);
			/*Float iRating = 0F;
			UserItemRating userItemRating = DBConnectionManager.getUserItemRatingByUserIdItemId(tUserId, itemId);
			if (userItemRating != null && userItemRating.getItemId()>0L){
				iRating = userItemRating.getRating();
			} else {
				System.out.println("Couldn't get rating for itemId:"+itemId+" and userId:"+tUserId);
			}*/
			Float tRating = userTrustMap.get(tUserId);
			if (tRating>0 && iRating>0F){
				numer += tRating*iRating;
				denom += tRating;
			}
			
		}
		if (denom>0){
			return numer/denom;
		}
		
		 return prediction;
		 
		 
	}

 private static UserTrustResponse getResp (final UserTrustRequest req){
	 
	 UserTrustResponse resp = null;
	 ExecutorService executor = Executors.newCachedThreadPool();
		Callable<Object> task = new Callable<Object>() {
		   public Object call() {
			   return ExtendedTidalTrustProcessor.getTrustRatings(req);
			   
		   }
		};
		Future<Object> future = executor.submit(task);
		try {
			resp = (UserTrustResponse)future.get(15, TimeUnit.SECONDS); 
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

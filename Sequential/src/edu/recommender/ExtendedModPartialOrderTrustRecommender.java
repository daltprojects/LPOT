package edu.recommender;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import edu.dal.DBConnectionManager;
import edu.loader.DataLoader;
import edu.models.RecommenderResult;
import edu.models.RecommenderResult.AlgorithmType;
import edu.models.RecommenderResult.TrustResult;
import edu.models.UserItemRating;
import edu.trust.ExtendedPartialOrderTrustProcessor;
import edu.trust.UserTrustRequest;
import edu.trust.UserTrustResponse;

public class ExtendedModPartialOrderTrustRecommender {

	public static List<RecommenderResult> getUserItemRecommendation(
			RecommendUserItemData recommendData, int maxDepth) {
		List<RecommenderResult> results = new ArrayList<RecommenderResult>();
		try {
			
			RecommenderResult partialOrderresult = new RecommenderResult();
			partialOrderresult.itemRating = -25F;
			partialOrderresult.algorithmType = AlgorithmType.PartialOrder;
			RecommenderResult modPartialOrderresult = new RecommenderResult();
			modPartialOrderresult.itemRating = -25F;
			modPartialOrderresult.algorithmType = AlgorithmType.ModPartialOrder;
			Long itemId = recommendData.getItemId();
			Long userId = recommendData.getUserId();
			DataLoader data = recommendData.getLoader();
			Float itemPrediction = -25F;// default

			UserTrustRequest req = new UserTrustRequest();
			req.setItemId(itemId);
			req.setUserId(userId);
			req.setLoader(data);
			req.setMaxDepth(maxDepth);

			// UserTrustResponse resp = getResp(req);
			UserTrustResponse resp = ExtendedPartialOrderTrustProcessor
					.getTrustRatings(req);
			// Tets

			if (resp == null) {
				partialOrderresult.trustResult = TrustResult.TIMED_OUT;
				partialOrderresult.itemRating = -21F;
				modPartialOrderresult.trustResult = TrustResult.TIMED_OUT;
				modPartialOrderresult.itemRating = -21F;
				results.add(partialOrderresult);
				results.add(modPartialOrderresult);
				return results; // Couldn't finish in specified time-frame
			}

			Map<Long, Float> userTrustMap = resp.getUserTrustRatingsMap();

			Float numer = 0.0F;
			Float denom = 0.0F;
			if (userTrustMap == null || userTrustMap.isEmpty()) {

				partialOrderresult.trustResult = TrustResult.NO_INFO;
				partialOrderresult.itemRating = -22F;

				modPartialOrderresult.trustResult = TrustResult.NO_INFO;
				modPartialOrderresult.itemRating = -22F;

				results.add(partialOrderresult);
				results.add(modPartialOrderresult);
				return results; // No common trusted users
			}

			int userTrustCount = 0;
			int userDistrustCount = 0;
			// Get the counts of users
			for (Long tUserId : userTrustMap.keySet()) {
				Float tRating = userTrustMap.get(tUserId);
				if (tRating > 0) {
					userTrustCount++;
				} else {
					userDistrustCount++;
				}
			}
			if (userTrustCount == userDistrustCount) {
				modPartialOrderresult.trustResult = TrustResult.AMBIGOUS;
				modPartialOrderresult.itemRating = -23F;
				// return result;
			} else if (userDistrustCount > userTrustCount) {
				modPartialOrderresult.trustResult = TrustResult.NO_INFO;
				modPartialOrderresult.itemRating = -24F;
				// return result;
			}

			for (Long tUserId : userTrustMap.keySet()) {
				// Float iRating =
				// data.getUserBoMap().get(tUserId).getItemRatingsMap().get(itemId);
				Float iRating = 0F;
				UserItemRating userItemRating = DBConnectionManager
						.getUserItemRatingByUserIdItemId(tUserId, itemId);
				if (userItemRating != null && userItemRating.getItemId() > 0L) {
					iRating = userItemRating.getRating();
				}
				Float tRating = userTrustMap.get(tUserId);
				if (iRating > 0F && tRating > 0) {
					numer += tRating * iRating;
					denom += tRating;
				}

			}
			if (denom > 0) {
				itemPrediction = numer / denom;
			}
			partialOrderresult.itemRating = itemPrediction;
			if (modPartialOrderresult.trustResult != TrustResult.NO_INFO
					&& modPartialOrderresult.trustResult != TrustResult.AMBIGOUS) {
				modPartialOrderresult.itemRating = itemPrediction;
			}
			results.add(modPartialOrderresult);
			results.add(partialOrderresult);

			return results;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return results;

	}

	
}

package edu.eval;

import java.util.List;

import edu.loader.DataLoader;
import edu.loader.EpinionsDataLoader;
import edu.models.UserItemRating;
import edu.recommender.RecommendUserItemData;
import edu.recommender.ResnicRecommender;

public class ResnicRecommedationEvaluator {

	/**
	 * Load epinions data Get list of user item ratings Leave one user item
	 * rating Get Leave one loader Call appropriate recommender with leave one
	 * data
	 */

	public static void evaluateLeaveOneEpinionsData() {
		DataLoader loader = EpinionsDataLoader.getInstance();
		List<UserItemRating> userItemList = loader.getUserItemRatings();

		for (UserItemRating userItemRating : userItemList) {
			LeaveOneUserItemData excludedData = new LeaveOneUserItemData();
			// excludedData.setItemId(userItemRating.getItemId());
			// excludedData.setUserId(userItemRating.getUserId());
			excludedData.setItemId(101L);
			excludedData.setUserId(1L);
			excludedData.setLoader(loader);
			DataLoader leaveOneLoader = new EpinionsLeaveOneDataManager(
					excludedData);
			RecommendUserItemData recommendData = new RecommendUserItemData();
			// recommendData.setItemId(userItemRating.getItemId());
			// recommendData.setUserId(userItemRating.getUserId());
			recommendData.setItemId(101L);
			recommendData.setUserId(1L);
			recommendData.setLoader(leaveOneLoader);
			Float prediction = ResnicRecommender
					.getUserItemRecommendation(recommendData);
			System.out.println(userItemRating.getUserId() + ","
					+ userItemRating.getItemId() + ","
					+ userItemRating.getRating() + "," + prediction);
		}

	}

	public static void main(String[] args) {
		evaluateLeaveOneEpinionsData();
	}
}

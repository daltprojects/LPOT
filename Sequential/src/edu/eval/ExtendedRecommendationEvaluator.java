package edu.eval;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.dal.DBConnectionManager;
import edu.loader.DataLoader;
import edu.loader.ExtendedEpinionsDataLoader;
import edu.models.UserItemRating;
import edu.recommender.ExtendedTidalTrustRecommender;
import edu.recommender.RecommendUserItemData;
import edu.trust.RecommendationResult;

public class ExtendedRecommendationEvaluator {

	/**
	 * Evaluator for extended epinions data set
	 * 
	 * @param userIds
	 * @param totalRatings
	 */
	public static void evaluateExtendedLeaveOneEpinionsData(List<Long> userIds,
			int totalRatings, int depth) {
		// DataLoader loader = EpinionsDataLoader.getInstance();
		DataLoader loader = ExtendedEpinionsDataLoader.getInstance();
		StringBuffer buffer = new StringBuffer();
		long start = System.currentTimeMillis();
		System.out
				.println("UserId, ItemId, ActualRating, PredictedRating, time, DB_hits");
		// int count = 0;
		for (Long userId : userIds) {
			List<UserItemRating> userItemRatings = DBConnectionManager
					.getUserItemRatings(userId);

			StringBuffer interBuffer = getExtendedPredictions(userItemRatings,
					loader, depth);
			buffer.append(interBuffer.toString());
			/*
			 * count++; if (count == 100){
			 * 
			 * writeToFile(buffer.toString(), totalRatings); buffer = new
			 * StringBuffer(); count=0; }
			 */
		}
		writeToFile(buffer.toString(), totalRatings, depth);
		long end = System.currentTimeMillis();
		System.out.println("time =" + (end - start));

	}

	private static StringBuffer getExtendedPredictions(
			List<UserItemRating> userItemRatings, DataLoader loader, int depth) {
		StringBuffer buffer = new StringBuffer();

		for (UserItemRating userItemRating : userItemRatings) {
			Long itemId = userItemRating.getItemId();
			Float actualRating = userItemRating.getRating();
			Long userId = userItemRating.getUserId();
			// DBConnectionManager.deleteUserItemRating(userItemRating);

			RecommendUserItemData recommendData = new RecommendUserItemData();
			recommendData.setItemId(itemId);
			recommendData.setUserId(userItemRating.getUserId());
			recommendData.setLoader(loader);

			long prevHits = DBConnectionManager.hits;

			try {
				// Float prediction1 = 0F;
				long start = System.currentTimeMillis();

				RecommendationResult prediction1 = ExtendedTidalTrustRecommender
						.getUserItemRecommendation(recommendData, depth);
				long end = System.currentTimeMillis();

				buffer.append(userId + "," + itemId + "," + actualRating + ","
						+ prediction1.getPrediction() + ","
						+ (prediction1.getDepthFound()) + "," + (end - start)
						+ "," + (DBConnectionManager.hits - prevHits));
			} catch (Throwable t) {
				t.printStackTrace();

				System.out
						.println("Exception in getting recommendations for userId:"
								+ userItemRating.getUserId()
								+ ":ItemId"
								+ userItemRating.getItemId()
								+ ";rating"
								+ userItemRating.getRating());
			}
			// DBConnectionManager.insertUserItemRating(userItemRating);
			buffer.append("\n");
		}
		return buffer;

	}

	private static void writeToFile(String str, Integer split, int depth) {
		BufferedWriter bufferWritter = null;
		try {

			File file = new File("TrustRatings_epinions_ext_cs_04_2014_split_"
					+ split + "_d" + depth + ".csv");
			// File file =new
			// File("TrustRatings_epinions_ext_contro_items_d2.txt");

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			// true = append file
			bufferWritter = new BufferedWriter(new FileWriter(file.getName(),
					true));

			bufferWritter.write(str);
			bufferWritter.close();

			System.out.println("Done");

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (bufferWritter != null) {
					bufferWritter.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	public static void main(String[] args) {

		for (int totalRatings = 1; totalRatings <= 1; totalRatings++) {
			BufferedReader reader = null;
			String line = null;
			List<Long> userIds = new ArrayList<Long>();
			try {
				reader = new BufferedReader(
						new FileReader(
								"/Users/dalthuru/Documents/Trust/Implementation/epinions/TrustAndDistrust/coldstart/users_"
										+ totalRatings + "itemrating.csv"));
				while ((line = reader.readLine()) != null) {
					String[] splits = line.split(",");
					if (null != splits) {

						userIds.add(new Long(splits[0]));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException io) {

					}
				}
			}

			// evaluateExtendedLeaveOneEpinionsData(userIds,619);
			evaluateExtendedLeaveOneEpinionsData(userIds, totalRatings, 3);
			// evaluateExtendedLeaveOneEpinionsData(userIds, totalRatings,2);
			System.out.println("Hit number is" + DBConnectionManager.hits);
		}

	}
}

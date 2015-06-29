package edu.eval;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import edu.loader.DataLoader;
import edu.loader.EpinionsDataLoader;
import edu.models.UserItemRating;
import edu.recommender.RecommendUserItemData;
import edu.recommender.ResnicRecommender;
import edu.recommender.TidalTrustRecommender;

public class TidalTrustRecommendationEvaluator {

	public static void evaluateLeaveOneEpinionsData() {
		DataLoader loader = EpinionsDataLoader.getInstance();
		List<UserItemRating> userItemList = loader.getUserItemRatings();
		int count = 0;
		int rCount = 0;
		long start = System.currentTimeMillis();
		StringBuffer buffer = new StringBuffer();
		int i = 1;
		int size = userItemList.size();
		System.out.println("Ti" + size);
		for (int j = 2001; j < userItemList.size(); j++) {
			UserItemRating userItemRating = userItemList.get(j);
			LeaveOneUserItemData excludedData = new LeaveOneUserItemData();
			excludedData.setItemId(userItemRating.getItemId());
			excludedData.setUserId(userItemRating.getUserId());
			excludedData.setLoader(loader);
			DataLoader leaveOneLoader = new EpinionsLeaveOneDataManager(
					excludedData);
			RecommendUserItemData recommendData = new RecommendUserItemData();
			recommendData.setItemId(userItemRating.getItemId());
			recommendData.setUserId(userItemRating.getUserId());
			recommendData.setLoader(leaveOneLoader);
			Float prediction2 = -2F;
			Float prediction3 = -2F;
			Float prediction4 = -2F;
			Float prediction1 = TidalTrustRecommender
					.getUserItemRecommendation(recommendData, 1);
			if (prediction1 == null || prediction1 <= 0F) {
				prediction2 = TidalTrustRecommender.getUserItemRecommendation(
						recommendData, 2);
			}
			/*
			 * if (prediction1 == null || prediction1 <=0F){ prediction2 =
			 * TidalTrustRecommender.getUserItemRecommendation(recommendData,2);
			 * } if (prediction2 != -2F && (prediction2 == null || prediction2
			 * <=0F)){ if (prediction2 == -3F){ prediction3 = -3F; } else{
			 * prediction3 =
			 * TidalTrustRecommender.getUserItemRecommendation(recommendData,3);
			 * } } if (prediction3 != -2F && (prediction3 == null || prediction3
			 * <=0F)){ if (prediction3 == -3F){ prediction4 = -3F; } else {
			 * prediction4 =
			 * TidalTrustRecommender.getUserItemRecommendation(recommendData,4);
			 * } }
			 */

			Float rPrediction = ResnicRecommender
					.getUserItemRecommendation(recommendData);
			buffer.append(userItemRating.getUserId() + ","
					+ userItemRating.getItemId() + ","
					+ userItemRating.getRating() + "," + rPrediction + ","
					+ prediction1 + "," + prediction2 + "," + prediction3 + ","
					+ prediction4);
			// System.out.println
			// (userItemRating.getUserId()+","+userItemRating.getItemId()+","+userItemRating.getRating()+","+rPrediction+","+prediction1+","+prediction2+","+prediction3+","+prediction4);
			buffer.append("\n");
			count++;
			if (count == 50) {
				rCount++;
				writeToFile(buffer.toString(), i);
				buffer = new StringBuffer();
				count = 0;
			}
			if (rCount == 10) {
				i++;
			}
		}
		long end = System.currentTimeMillis();
		System.out.println("TidalCount:" + count + ";ResnicCount:" + rCount
				+ ":time =" + (end - start));

	}

	private static void writeToFile(String str, Integer split) {
		BufferedWriter bufferWritter = null;
		try {
			File file = new File("TrustRatings_ph2_" + split + ".txt");
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
		evaluateLeaveOneEpinionsData();
	}
}

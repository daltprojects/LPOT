package edu.eval;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.dal.DBConnectionManager;
import edu.dal.MiscHelper;
import edu.models.UserItemRating;

/**
 * Calculates the controversial items for Standard and Extended Epinions
 * dataset. Ratings for an item having standard deviation greater than 1.5.
 * 
 */
public class ControversialItemsCalculator {

	enum EpinionsType {
		Standard, Extended
	}

	public static void calculateControversialUsers(String fileName,
			EpinionsType epinionsType) {
		BufferedReader reader = null;
		String line = null;
		List<UserItemRating> controversialItems = new ArrayList<UserItemRating>();
		try {

			reader = new BufferedReader(new FileReader(fileName));
			while ((line = reader.readLine()) != null) {

				if (null != line && line.trim().length() > 0) {
					String[] splits = line.split(",");
					Long itemId = new Long(splits[0]);
					isControversialItem(itemId, controversialItems);

				}
			}

			StringBuffer buffer = new StringBuffer();
			for (UserItemRating uIRating : controversialItems) {
				buffer.append(uIRating.getUserId() + "," + uIRating.getItemId()
						+ "," + uIRating.getRating());
				buffer.append("\n");
			}
			System.out.println("Size is" + controversialItems.size());
			MiscHelper.writeToFile(buffer,
					"Controverial_user_items_standard.csv");
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
	}

	public static void main(String[] args) {
		String standardFileName = "/Users/dalthuru/Documents/Trust/Implementation/epinions/OnlyTrust/ItemsGreaterThan20Ratings.csv";
		// String standardFileName =
		// "/Users/dalthuru/Documents/Trust/Implementation/epinions/TrustAndDistrust/ItemsGreaterThan20Ratings.csv";

		calculateControversialUsers(standardFileName, EpinionsType.Standard);
	}

	public static boolean isControversialItem(Long itemId,
			List<UserItemRating> controversialItems) {
		boolean retVal = false;
		List<UserItemRating> userItemRatings = DBConnectionManager
				.getUserItemRatingsForItemId(itemId);
		List<Float> ratings = new ArrayList<Float>();
		if (null != userItemRatings) {
			for (UserItemRating uIRating : userItemRatings) {
				ratings.add(uIRating.getRating());
			}
		}
		Float sd = getStandardDeviation(ratings);
		if (sd > 1.5) {
			controversialItems.addAll(userItemRatings);
			return true;
		}

		return retVal;

	}

	public static Float getStandardDeviation(List<Float> ratings) {

		if (ratings == null || ratings.size() < 2) {
			return 0.0F;
		}

		Float average = 0.0F;
		int size = ratings.size();
		for (Float rating : ratings) {
			average += rating;
		}
		average = average / size;
		Double interValue = 0.0;
		for (Float rating : ratings) {
			interValue += (rating - average) * (rating - average);
		}
		return (float) (Math.sqrt(interValue / size));

	}

}

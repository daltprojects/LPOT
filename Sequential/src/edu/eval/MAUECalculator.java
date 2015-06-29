package edu.eval;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class MAUECalculator {

	static Map<RecommenderType, MeanRatingsError> meanRatingsMap = new HashMap<RecommenderType, MeanRatingsError>();
	static Map<RecommenderType, Map<Long, MeanRatingsError>> meanUserRatingsMap = new HashMap<RecommenderType, Map<Long, MeanRatingsError>>();
	static Set<Long> userIds = new TreeSet<Long>();
	static int totalRecords = 0;

	public enum RecommenderType {
		CF, Tidal, PartialOrder, ModPartialOrder
	}

	public static void main(String[] args) {
		// evaluateLeaveOneEpinionsData();
		StringBuffer buffer = new StringBuffer();
		for (int totalRatings = 1; totalRatings <= 1; totalRatings++) {
			BufferedReader reader = null;
			String line = null;

			try {

				reader = new BufferedReader(
						new FileReader(
								"/Users/dalthuru/Documents/Trust/Implementation/Sequential/src/TrustRatings_epinions_cs_ext_1_5_d2_cons.csv"));
				while ((line = reader.readLine()) != null) {

					if (null != line && line.trim().length() > 0) {
						if (line.startsWith("User")) {
							continue;
						}
						totalRecords++;
						String[] splits = line.split(",");
						Long userId = new Long(splits[0]);
						userIds.add(userId);
						float actualRating = new Float(splits[2]);
						// float cfRating = new Float (splits[3]);
						float tidalRating = new Float(splits[3]);
						float partialOrderRating = new Float(splits[4]);
						float modPartialOrderRating = new Float(splits[5]);

						/*
						 * if (cfRating > -1){ // Ratings error MeanRatingsError
						 * error = meanRatingsMap.get(RecommenderType.CF); if
						 * (error == null){ error = new MeanRatingsError();
						 * 
						 * } error.count = error.count+1; error.error =
						 * error.error + Math.abs(cfRating-actualRating);
						 * meanRatingsMap.put(RecommenderType.CF, error);
						 * 
						 * // User Mean
						 * 
						 * Map<Long,MeanRatingsError> userError =
						 * meanUserRatingsMap.get(RecommenderType.CF); if (null
						 * == userError){ userError = new
						 * HashMap<Long,MeanRatingsError> (); } MeanRatingsError
						 * meanUserError = userError.get(userId); if (null ==
						 * meanUserError){ meanUserError = new
						 * MeanRatingsError(); } meanUserError.count =
						 * meanUserError.count+1; meanUserError.error =
						 * meanUserError.error +
						 * Math.abs(cfRating-actualRating);
						 * userError.put(userId, meanUserError);
						 * meanUserRatingsMap.put(RecommenderType.CF,
						 * userError); }
						 */

						if (tidalRating > 0) {
							// Ratings error
							MeanRatingsError error = meanRatingsMap
									.get(RecommenderType.Tidal);
							if (error == null) {
								error = new MeanRatingsError();

							}
							error.count = error.count + 1;
							error.error = error.error
									+ Math.abs(tidalRating - actualRating);
							meanRatingsMap.put(RecommenderType.Tidal, error);

							// User Mean

							Map<Long, MeanRatingsError> userError = meanUserRatingsMap
									.get(RecommenderType.Tidal);
							if (null == userError) {
								userError = new HashMap<Long, MeanRatingsError>();
							}
							MeanRatingsError meanUserError = userError
									.get(userId);
							if (null == meanUserError) {
								meanUserError = new MeanRatingsError();
							}
							meanUserError.count = meanUserError.count + 1;
							meanUserError.error = meanUserError.error
									+ Math.abs(tidalRating - actualRating);
							userError.put(userId, meanUserError);
							meanUserRatingsMap.put(RecommenderType.Tidal,
									userError);
						}

						if (partialOrderRating > -1) {
							// Ratings error
							MeanRatingsError error = meanRatingsMap
									.get(RecommenderType.PartialOrder);
							if (error == null) {
								error = new MeanRatingsError();

							}
							error.count = error.count + 1;
							error.error = error.error
									+ Math.abs(partialOrderRating
											- actualRating);
							meanRatingsMap.put(RecommenderType.PartialOrder,
									error);

							// User Mean

							Map<Long, MeanRatingsError> userError = meanUserRatingsMap
									.get(RecommenderType.PartialOrder);
							if (null == userError) {
								userError = new HashMap<Long, MeanRatingsError>();
							}
							MeanRatingsError meanUserError = userError
									.get(userId);
							if (null == meanUserError) {
								meanUserError = new MeanRatingsError();
							}
							meanUserError.count = meanUserError.count + 1;
							meanUserError.error = meanUserError.error
									+ Math.abs(partialOrderRating
											- actualRating);
							userError.put(userId, meanUserError);
							meanUserRatingsMap.put(
									RecommenderType.PartialOrder, userError);
						}

						if (modPartialOrderRating > -1) {
							// Ratings error
							MeanRatingsError error = meanRatingsMap
									.get(RecommenderType.ModPartialOrder);
							if (error == null) {
								error = new MeanRatingsError();

							}
							error.count = error.count + 1;
							error.error = error.error
									+ Math.abs(modPartialOrderRating
											- actualRating);
							meanRatingsMap.put(RecommenderType.ModPartialOrder,
									error);

							// User Mean

							Map<Long, MeanRatingsError> userError = meanUserRatingsMap
									.get(RecommenderType.ModPartialOrder);
							if (null == userError) {
								userError = new HashMap<Long, MeanRatingsError>();
							}
							MeanRatingsError meanUserError = userError
									.get(userId);
							if (null == meanUserError) {
								meanUserError = new MeanRatingsError();
							}
							meanUserError.count = meanUserError.count + 1;
							meanUserError.error = meanUserError.error
									+ Math.abs(modPartialOrderRating
											- actualRating);
							userError.put(userId, meanUserError);
							meanUserRatingsMap.put(
									RecommenderType.ModPartialOrder, userError);
						}

					}

				}

				// Print Mean Absolute ratings error and coverage
				System.out.println("Total Ratings:" + totalRecords);
				// printMeanRatingsStats(meanRatingsMap.get(RecommenderType.CF),
				// "CF");
				printMeanRatingsStats(
						meanRatingsMap.get(RecommenderType.Tidal), "Tidal");
				printMeanRatingsStats(
						meanRatingsMap.get(RecommenderType.PartialOrder),
						"Partial Order");
				printMeanRatingsStats(
						meanRatingsMap.get(RecommenderType.ModPartialOrder),
						"Partial Order Variation");

				// Print Mean Absolute user ratings error and coverage
				System.out.println("Total users:" + userIds.size());
				// printMeanUserRatingsStats(meanUserRatingsMap.get(RecommenderType.CF),"CF");
				printMeanUserRatingsStats(
						meanUserRatingsMap.get(RecommenderType.Tidal), "Tidal");
				printMeanUserRatingsStats(
						meanUserRatingsMap.get(RecommenderType.PartialOrder),
						"Partial Order");
				printMeanUserRatingsStats(
						meanUserRatingsMap.get(RecommenderType.ModPartialOrder),
						"Partial Order Variation");

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
		// writeToFile(buffer);
	}

	private static void printMeanRatingsStats(MeanRatingsError mRError,
			String type) {
		System.out.println(type + " Ratings count is:" + mRError.count);
		System.out.println(type + " Ratings error is:" + mRError.error
				/ mRError.count);
		System.out.println(type + " Ratings coverage percentage is:"
				+ ((new Float(mRError.count) / new Float(totalRecords)) * 100));
	}

	private static void printMeanUserRatingsStats(
			Map<Long, MeanRatingsError> users, String type) {
		System.out.println(type + " User Ratings count is:"
				+ users.keySet().size());
		System.out.println(type + " Ratings MAUE error is:"
				+ (getTotalError(users)) / users.keySet().size());
		System.out
				.println(type
						+ " Ratings MAUE coverage percentage is:"
						+ (new Float(users.keySet().size()) / new Float(userIds
								.size())) * 100);
	}

	private static Float getTotalError(Map<Long, MeanRatingsError> meanCFUsers) {
		Float totalError = 0F;
		for (MeanRatingsError mRError : meanCFUsers.values()) {
			totalError += mRError.error / mRError.count;
		}
		return totalError;
	}

	private static void writeToFile(StringBuffer buffer) {
		BufferedWriter bufferWritter = null;
		try {

			File file = new File("TrustRatings_epinions_cs_cons.csv");

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			// true = append file
			bufferWritter = new BufferedWriter(new FileWriter(file.getName(),
					true));

			bufferWritter.write(buffer.toString());
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

	static class MeanRatingsError {

		public Float error = 0.0F;
		public int count = 0;
	}

}

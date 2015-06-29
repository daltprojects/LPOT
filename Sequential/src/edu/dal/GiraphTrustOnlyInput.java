package edu.dal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GiraphTrustOnlyInput {

	public static void main(String[] args) {

		BufferedReader reader = null;
		String line = null;
		Map<Long, String> adjacencyMap = new HashMap<Long, String>();
		try {
			reader = new BufferedReader(
					new FileReader(
							"/Users/dalthuru/Documents/Trust/Implementation/epinions/TrustAndDistrust/user_rating.txt"));
			// reader = new BufferedReader(new
			// FileReader("/Users/dalthuru/Documents/Trust/Implementation/Sequential/src/TrustRatings_epinions_2_CS_619.txt"));
			while ((line = reader.readLine()) != null) {
				if (null != line && line.trim().length() > 0) {
					String[] splits = line.trim().split("\\s+");
					Long userId = Long.valueOf(splits[0]);
					Long tUserId = Long.valueOf(splits[1]);
					Double tValue = Double.valueOf(splits[2]);
					StringBuffer buf;
					if (adjacencyMap.containsKey(userId)) {
						buf = new StringBuffer(adjacencyMap.get(userId));
						buf.append("\t" + tUserId + "\t" + tValue);
					} else {
						buf = new StringBuffer();
						buf.append(userId + "\t" + "0.0" + "\t" + tUserId
								+ "\t" + tValue);
					}
					adjacencyMap.put(userId, buf.toString());
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
		StringBuffer toWrite = new StringBuffer();
		int count = 0;
		boolean start = true;
		for (String buf : adjacencyMap.values()) {

			if (count == 500) {
				// MiscHelper.writeToFile(toWrite,
				// "/Users/dalthuru/Documents/Trust/Implementation/epinions/OnlyTrust/giraph_trust_only.txt");
				MiscHelper.writeToFile(toWrite, "giraph_ext_tab.txt");
				toWrite.setLength(0);
				count = 0;
			}
			if (!start) {
				toWrite.append("\n");
			}
			toWrite.append(buf);

			count++;
			start = false;
		}
		// writeToFile(adjacencyMap);
	}

}

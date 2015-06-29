package edu.dal;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MiscHelper {

	public static void writeToFile(StringBuffer buffer, String fileName) {
		BufferedWriter bufferWritter = null;
		try {
			File file = new File(fileName);

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
}

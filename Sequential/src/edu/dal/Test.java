package edu.dal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Test {

	public static void main(String[] args) {

		/*
		 * String line=
		 * "1 0.0 384 1.0 215 1.0 140 1.0 310 1.0 95 1.0 302 1.0 185 1.0 71 1.0 150 1.0 225 1.0 159 1.0 117 1.0 156 1.0 265 1.0 60 1.0 149 1.0 61 1.0 386 1.0 357 1.0 381 1.0 389 1.0 87 1.0 106 1.0 122 1.0 338 1.0 104 1.0 96 1.0 242 1.0 281 1.0 316 1.0 182 1.0 379 1.0 189 1.0 82 1.0 85 1.0 66 1.0 231 1.0 126 1.0 127 1.0 174 1.0 373 1.0 318 1.0 377 1.0 68 1.0 154 1.0 354 1.0 308 1.0 276 1.0 262 1.0 270 1.0 334 1.0 190 1.0 331 1.0 387 1.0 224 1.0 243 1.0 125 1.0 317 1.0 245 1.0 135 1.0 52 1.0 267 1.0 50 1.0 247 1.0 378 1.0 33 1.0 285 1.0 146 1.0 374 1.0 327 1.0 121 1.0 206 1.0 15 1.0 72 1.0 124 1.0 58 1.0 288 1.0 358 1.0 383 1.0 116 1.0 233 1.0 81 1.0 326 1.0 313 1.0 382 1.0 83 1.0 199 1.0 210 1.0 372 1.0 209 1.0 323 1.0 31 1.0 19 1.0 208 1.0 367 1.0 375 1.0 366 1.0 360 1.0 133 1.0 329 1.0 65 1.0 238 1.0 111 1.0 16 1.0 186 1.0 40 1.0 341 1.0 201 1.0 103 1.0 311 1.0 197 1.0 97 1.0 12 1.0 275 1.0 34 1.0 144 1.0 193 1.0 269 1.0 322 1.0 355 1.0 137 1.0 277 1.0 255 1.0 8 1.0 131 1.0 234 1.0 119 1.0 321 1.0 307 1.0 312 1.0 99 1.0 320 1.0 51 1.0 250 1.0 6 1.0 342 1.0 5 1.0 98 1.0 339 1.0 207 1.0 368 1.0 203 1.0 45 1.0 178 1.0 145 1.0 165 1.0 352 1.0 62 1.0 287 1.0 75 1.0 337 1.0 108 1.0 351 1.0 260 1.0 279 1.0 169 1.0 298 1.0 361 1.0 344 1.0 74 1.0 175 1.0 222 1.0 22 1.0 191 1.0 227 1.0 363 1.0 69 1.0 139 1.0 252 1.0 274 1.0 160 1.0 168 1.0 179 1.0 218 1.0 194 1.0 340 1.0 148 1.0 301 1.0 105 1.0 79 1.0 26 1.0 204 1.0 306 1.0 123 1.0 93 1.0 55 1.0 261 1.0 4 1.0 9 1.0 32 1.0 30 1.0 349 1.0 101 1.0 151 1.0 226 1.0 113 1.0 273 1.0 167 1.0 136 1.0 170 1.0 3 1.0 345 1.0 346 1.0 376 1.0 303 1.0 110 1.0 294 1.0 128 1.0 236 1.0 158 1.0 258 1.0 187 1.0 319 1.0 347 1.0 295 1.0 177 1.0 102 1.0 43 1.0 142 1.0 254 1.0 120 1.0 216 1.0 264 1.0 268 1.0 230 1.0 7 1.0 24 1.0 164 1.0 380 1.0 365 1.0 330 1.0 77 1.0 155 1.0 143 1.0 36 1.0 212 1.0 46 1.0 263 1.0 253 1.0 129 1.0 271 1.0 56 1.0 293 1.0 49 1.0 27 1.0 259 1.0 266 1.0 188 1.0 78 1.0 356 1.0 17 1.0 112 1.0 241 1.0 198 1.0 283 1.0 181 1.0 385 1.0 39 1.0 41 1.0 94 1.0 217 1.0 10 1.0 200 1.0 42 1.0 289 1.0 309 1.0 364 1.0 70 1.0 286 1.0 67 1.0 157 1.0 54 1.0 240 1.0 214 1.0 92 1.0 246 1.0 332 1.0 239 1.0 219 1.0 213 1.0 173 1.0 176 1.0 84 1.0 257 1.0 153 1.0 251 1.0 88 1.0 388 1.0 28 1.0 292 1.0 161 1.0 109 1.0 163 1.0 21 1.0 107 1.0 221 1.0 232 1.0 44 1.0 25 1.0 296 1.0 180 1.0 114 1.0 183 1.0 91 1.0 53 1.0 57 1.0 290 1.0 196 1.0 195 1.0 350 1.0 132 1.0 256 1.0 324 1.0 228 1.0 278 1.0 205 1.0 29 1.0 130 1.0 325 1.0 18 1.0 192 1.0 343 1.0 35 1.0 172 1.0 166 1.0 229 1.0 73 1.0 147 1.0 304 1.0 370 1.0 23 1.0 249 1.0 100 1.0 244 1.0 184 1.0 89 1.0 223 1.0 90 1.0 59 1.0 64 1.0 134 1.0 202 1.0 300 1.0 162 1.0 138 1.0 211 1.0 359 1.0 63 1.0 348 1.0 141 1.0 362 1.0 80 1.0 152 1.0 86 1.0 336 1.0 282 1.0 353 1.0 20 1.0 280 1.0 315 1.0 37 1.0 291 1.0 47 1.0 335 1.0 297 1.0 220 1.0 333 1.0 38 1.0 76 1.0 11 1.0 115 1.0 299 1.0 13 1.0 272 1.0 237 1.0 284 1.0 235 1.0 2 1.0 369 1.0 328 1.0 171 1.0 305 1.0 14 1.0 314 1.0 118 1.0 248 1.0 48 1.0 371 1.0"
		 * ; String [] values = line.split(null); if ((values.length < 2) ||
		 * (values.length % 2 != 0)) { throw new IllegalArgumentException(
		 * "Line did not split correctly: " + line); }
		 */
		// evaluateLeaveOneEpinionsData();
		StringBuffer buffer = new StringBuffer();
		for (int totalRatings = 619; totalRatings <= 619; totalRatings++) {
			BufferedReader reader = null;
			String line = null;

			try {
				reader = new BufferedReader(
						new FileReader(
								"/Users/dalthuru/Documents/Trust/Implementation/Sequential/src/TrustRatings_epinions_ext_cs_"
										+ totalRatings + ".txt"));
				// reader = new BufferedReader(new
				// FileReader("/Users/dalthuru/Documents/Trust/Implementation/Sequential/src/TrustRatings_epinions_2_CS_619.txt"));
				while ((line = reader.readLine()) != null) {
					if (null != line && line.trim().length() > 0) {
						String[] splits = line.split(",");
						String tidalRating = splits[3];
						String partialRating = splits[4];
						if (null != tidalRating
								&& (new Float(tidalRating)).floatValue() > 0
								&& !tidalRating.equals(partialRating)) {
							buffer.append(line);
							buffer.append("\n");
						}
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

		}
		writeToFile(buffer);
	}

	private static void writeToFile(StringBuffer buffer) {
		BufferedWriter bufferWritter = null;
		try {

			File file = new File("Temp.txt");

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

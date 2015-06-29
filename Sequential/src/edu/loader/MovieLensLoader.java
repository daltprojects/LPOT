package edu.loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import edu.movielens.Movie;
import edu.movielens.UserRating;

public class MovieLensLoader {

	private static MovieLensLoader movieLensInstance = null;
	private static List<Movie> movies = null;
	private static List<UserRating> userRatings = null;
	private static HashMap<Long, List<UserRating>> userRatingsHash = new HashMap<Long, List<UserRating>>();
	private static Set<Long> users = new TreeSet<Long>();
	private static HashMap<Long, List<Long>> userMovieHash = new HashMap<Long, List<Long>>();

	private MovieLensLoader() {
	};

	public static MovieLensLoader getInstance() {
		if (movieLensInstance == null) {
			movieLensInstance = new MovieLensLoader();
		}
		if (null == movies) {
			loadMovies();
		}
		if (null == userRatings) {
			loadUserRatings();
		}
		return movieLensInstance;
	}

	private static void loadMovies() {

		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(
					"MovielensDataset/movies.dat")));
			if (null != reader) {
				String movie;
				movies = new ArrayList<Movie>();
				while ((movie = reader.readLine()) != null) {
					String[] splits = movie.split("::");
					if (null != splits && splits.length == 3) {
						Movie movieData = new Movie();
						movieData.setId(new Long(splits[0]));
						// setTitle and yeat
						setTitleAndYear(splits[1], movieData);
						setGenres(splits[2], movieData);
						for (String genre : movieData.getGenre()) {
							System.out.println("Genre is" + genre);

						}
						movies.add(movieData);

					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<Movie> getMovieData() {
		return movies;
	}

	public Set<Long> getUsers() {
		return users;
	}

	public List<UserRating> getUserRatingsData() {
		return userRatings;
	}

	public HashMap<Long, List<Long>> getUserMovieHash() {
		return userMovieHash;
	}

	private static void loadUserRatings() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(
					"MovielensDataset/ratings.dat")));
			if (null != reader) {
				String userRating;
				userRatings = new ArrayList<UserRating>();
				while ((userRating = reader.readLine()) != null) {
					String[] splits = userRating.split("::");
					if (null != splits && splits.length == 4) {
						UserRating userRatingData = new UserRating();
						userRatingData.setId(new Long(splits[0]));
						userRatingData.setMovieId(new Long(splits[1]));
						userRatingData.setRating(new Float(splits[2]));
						userRatingData.setTimeStamp(new Long(splits[3]));
						if (userMovieHash.get(userRatingData.getId()) != null) {
							List<Long> value = new ArrayList<Long>();
							value.add(userRatingData.getMovieId());
							userMovieHash.put(userRatingData.getId(), value);
						} else {
							userMovieHash.get(userRatingData.getId()).add(
									userRatingData.getMovieId());
						}
						if (userRatingsHash.get(userRatingData.getId()) != null) {
							List<UserRating> value = new ArrayList<UserRating>();
							value.add(userRatingData);
							userRatingsHash.put(userRatingData.getId(), value);
						} else {
							userRatingsHash.get(userRatingData.getId()).add(
									userRatingData);
						}
						users.add(userRatingData.getId());
						userRatings.add(userRatingData);

					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public HashMap<Long, List<UserRating>> getUserRatingsHash() {
		return userRatingsHash;
	}

	private static void setTitleAndYear(String data, Movie movie) {
		if (null == data || null == movie) {
			System.out.println("Invalid arguments to setTitleAndYear");
		}
		String[] splits = data.split("(\\()");
		if (null != splits && splits.length == 2) {
			movie.setTitle(splits[0].trim());
			String year = splits[1].replaceAll("[^\\d]", "");
			movie.setYear(new Integer(year));
		}
	}

	private static void setGenres(String data, Movie movie) {
		if (null == data || null == movie) {
			System.out.println("Invalid arguments to setGenres");
		}
		String[] splits = data.split("(\\|)");
		List<String> genres = Arrays.asList(splits);
		movie.setGenre(genres);
	}

	public static void main(String[] args) {
		MovieLensLoader movieLensLoader = MovieLensLoader.getInstance();
		System.out.println("Number of movies " + movies.size());
		System.out.println("Number of userRatings " + userRatings.size());
	}

}

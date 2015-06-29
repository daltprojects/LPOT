package edu.movielens;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import edu.loader.MovieLensLoader;
import edu.similarity.UserSimilarityData;

public class Similarity {
	
	private static List<UserSimilarityData> userSimilarities = new ArrayList<UserSimilarityData>();
	private static MovieLensLoader loader = MovieLensLoader.getInstance();
	private static HashMap<Long, List<Long>> userMovies = loader.getUserMovieHash();
	private static HashMap<Long,List<UserRating>> userRatingsHash = loader.getUserRatingsHash();
	private static Similarity similarity = null;
	
	private Similarity(){};
	
	public static Similarity getInstance(){
		if (null == similarity){
			similarity = new Similarity();
			
		}
		if (null == userSimilarities){
			userSimilarities = new ArrayList<UserSimilarityData>();
			loadSimilarityMatrix();
		}
		return similarity;
	}
	
	private static void loadSimilarityMatrix(){
			
		Set<Long> userIds = loader.getUsers();	
		
		for (long userId : userIds){
			UserSimilarityData similarityData = new UserSimilarityData(userId);
			for (long simUserId : userIds){
				if (simUserId != userId){
					float pearsonCoeff = getPearsonCoeff(userId, simUserId);
					similarityData.setSimilarity(simUserId, pearsonCoeff);
				}
			}
			
			userSimilarities.add(similarityData);
		}
	}
	
	private static Float getPearsonCoeff(Long user1, Long user2){
		
		if (null == user1 || null == user2){
			return 0.0f;
		}
		List<Long> user1Movies = userMovies.get(user1);
		List<Long> user2Movies = userMovies.get(user2);
		List<Long> matchedMovies = new ArrayList<Long>();
		for (Long user1Movie : user1Movies){
			if (user2Movies.contains(user1Movie)){
				matchedMovies.add(user1Movie);
			}
		}
		if (matchedMovies.size()==0){
			return 0.0f;
		}
		int matchLen = matchedMovies.size();
		double sum1=0.0f;
		double sum2=0.0f;
		double sum1Sq=0.0f;
		double sum2Sq=0.0f;
		double sumProd=0.0f;
		for (Long matchedMovie : matchedMovies){
			sum1+= getRating(user1, matchedMovie);
			sum2+= getRating(user2, matchedMovie);
			sum1Sq+= Math.pow(getRating(user1, matchedMovie),2);
			sum2Sq+= Math.pow(getRating(user1, matchedMovie),2);
			sumProd+=getRating(user1, matchedMovie)*getRating(user2, matchedMovie);
		}
		double numer = sumProd-(sum1*sum2/matchLen);
		double denom = Math.sqrt((sum1Sq-(Math.pow(sum1,2))/2)*(sum2Sq-(Math.pow(sum2,2))/2));
		if(denom==0){
			return 0.0f;
		} else {
			return (float)(numer/denom);
		}
		

		
	}
	
	private static float getRating(Long userId, Long movieId){
		
		List<UserRating> userRatings = userRatingsHash.get(userId);
		for (UserRating userRating : userRatings){
			if (userRating.getMovieId()==movieId.longValue()){
				return userRating.getRating();
			}
			
		}
		return 0.0f;
	}
	
	public static void main (String[] args){
		Similarity sim = Similarity.getInstance();
	}
	
}

package edu.similarity;

import java.util.HashMap;
import java.util.Map;

public class UserSimilarityData {
	
	private long id;
	
	public UserSimilarityData(long id){
		this.id = id;
	}
	
	private Map<Long, Float> similarity = new HashMap<Long, Float>();
	
	public Map<Long, Float> getSimilarity(){
		return similarity;
	}
	
	public void setSimilarity(long userId, float sim){
		similarity.put(userId, sim);
	}
	
}

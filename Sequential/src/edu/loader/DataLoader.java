package edu.loader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.models.UserBo;
import edu.models.UserItemRating;
import edu.models.UserTrustRating;

public interface DataLoader {
	
	public List<UserItemRating> getUserItemRatings();
	public HashMap<Long, List<UserItemRating>> getUserItemRatingsHash();
	public Set<Long> getUsers();
	public  HashMap<Long, List<Long>> getUserItemHash();
	public  HashMap<Long, List<Long>> getItemUserHash();
	public List<UserTrustRating> getUserTrustRatings();
	public  Map<Long, List<UserTrustRating>> getUserTrustRatingsHash();
	public  Map<Long, UserBo> getUserBoMap();
}

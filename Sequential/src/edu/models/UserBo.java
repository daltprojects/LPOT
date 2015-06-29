package edu.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserBo implements Cloneable {
	
	private Long userId;
	
	private List<Long> itemsRated = new ArrayList<Long>();
	
	private List<Long> trustedUsers = new ArrayList<Long>();
	
	private Map<Long, Float> itemRatingsMap = new HashMap<Long, Float>();
	
	private Map<Long, Float> userTrustMap = new HashMap<Long, Float>();

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public List<Long> getItemsRated() {
		if (itemsRated == null){
			itemsRated = new ArrayList<Long>();
		}
		return itemsRated;
	}

	public void setItemsRated(List<Long> itemsRated) {
		this.itemsRated = itemsRated;
	}

	public List<Long> getTrustedUsers() {
		if (trustedUsers == null){
			trustedUsers = new ArrayList<Long>();
		}
		return trustedUsers;
	}

	public void setTrustedUsers(List<Long> trustedUsers) {
		this.trustedUsers = trustedUsers;
	}

	public Map<Long, Float> getItemRatingsMap() {
		if (itemRatingsMap == null){
			itemRatingsMap = new HashMap<Long, Float>();
		}
		return itemRatingsMap;
	}

	public void setItemRatingsMap(Map<Long, Float> itemRatingsMap) {
		this.itemRatingsMap = itemRatingsMap;
	}

	public Map<Long, Float> getUserTrustMap() {
		if (userTrustMap == null){
			userTrustMap = new HashMap<Long, Float>();
		}
		return userTrustMap;
	}

	public void setUserTrustMap(Map<Long, Float> userTrustMap) {
		this.userTrustMap = userTrustMap;
	}
	
	public void addToRatedItems (Long rItemId){
		this.itemsRated.add(rItemId);
	}
	
	
	public void addTrustedUser (Long tUserId){
		this.trustedUsers.add(tUserId);
	}
	
	public void addToUserTrustMap (UserTrustRating trustRating){
		this.userTrustMap.put(trustRating.getOtherUserId(), trustRating.getTrustRating());
	}
	
	public void addToItemRatingsMap (UserItemRating itemtRating){
		this.itemRatingsMap.put(itemtRating.getItemId(), itemtRating.getRating());
	}
	
	public boolean equals (UserBo user){
		return this.getUserId().longValue() == user.getUserId().longValue();
	}
	
	
	public Object clone(){
		 try {
	        return super.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
		 System.out.println("UserBo can't be cloned");
		 return null;
	    
	}
	
	

}

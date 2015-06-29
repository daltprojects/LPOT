package edu.models;

public class UserTrustRating {
	
	private long userId;
	private long otherUserId;
	private float trustRating;
	
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public long getOtherUserId() {
		return otherUserId;
	}
	public void setOtherUserId(long otherUserId) {
		this.otherUserId = otherUserId;
	}
	public float getTrustRating() {
		return trustRating;
	}
	public void setTrustRating(float trustRating) {
		this.trustRating = trustRating;
	}
	

}

package edu.trust;

import edu.loader.DataLoader;

public class UserTrustRequest {
	
	private DataLoader loader;
	private Long userId;
	private Long itemId;
	private int maxDepth;
	
	private float minThresholdForTrust = Float.MIN_VALUE; 
	
	
	public int getMaxDepth() {
		return maxDepth;
	}
	public void setMaxDepth(int maxDepth) {
		this.maxDepth = maxDepth;
	}
	public DataLoader getLoader() {
		return loader;
	}
	public void setLoader(DataLoader loader) {
		this.loader = loader;
	}
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public Long getItemId() {
		return itemId;
	}
	public void setItemId(Long itemId) {
		this.itemId = itemId;
	}
	
	public float getMinThresholdForTrust() {
		return minThresholdForTrust;
	}
	public void setMinThresholdForTrust(float minThresholdForTrust) {
		this.minThresholdForTrust = minThresholdForTrust;
	}
}

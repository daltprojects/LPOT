package edu.trust;

import java.util.Map;

public class UserTrustResponse {
	
	private long requestedUserId;
	private long requestedItemId;
	
	private int depthFound;
	
	private Map<Long, Float> userTrustRatingsMap;

	public long getRequestedUserId() {
		return requestedUserId;
	}

	public void setRequestedUserId(long requestedUserId) {
		this.requestedUserId = requestedUserId;
	}

	public long getRequestedItemId() {
		return requestedItemId;
	}

	public void setRequestedItemId(long requestedItemId) {
		this.requestedItemId = requestedItemId;
	}

	public int getDepthFound() {
		return depthFound;
	}

	public void setDepthFound(int depthFound) {
		this.depthFound = depthFound;
	}

	public Map<Long, Float> getUserTrustRatingsMap() {
		return userTrustRatingsMap;
	}

	public void setUserTrustRatingsMap(Map<Long, Float> userTrustRatingsMap) {
		this.userTrustRatingsMap = userTrustRatingsMap;
	}
	
	

}

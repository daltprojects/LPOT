package edu.eval;

import edu.loader.DataLoader;

public class LeaveOneUserItemData {
	
	private DataLoader loader;
	private Long userId;
	private Long itemId;
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
	
	
}

package edu.eval;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.loader.DataLoader;
import edu.models.UserBo;
import edu.models.UserItemRating;
import edu.models.UserTrustRating;

public class EpinionsLeaveOneDataManager implements DataLoader {

	private LeaveOneUserItemData excludedData;

	private List<UserItemRating> userItemRatings = null;
	private HashMap<Long, List<UserItemRating>> userItemRatingsHash = null;
	private HashMap<Long, List<Long>> localUserItemsHash = null;
	private HashMap<Long, List<Long>> localItemUsersHash = null;
	private Map<Long, UserBo> localUserBoMap = null;

	public EpinionsLeaveOneDataManager(LeaveOneUserItemData excludededData) {

		this.excludedData = excludededData;

	}

	public List<UserItemRating> getUserItemRatings() {
		List<UserItemRating> ratings = excludedData.getLoader()
				.getUserItemRatings();
		if (userItemRatings == null) {
			userItemRatings = new ArrayList<UserItemRating>();
			for (UserItemRating rating : ratings) {
				userItemRatings.add(rating);
			}
			for (UserItemRating rating : userItemRatings) {
				if (rating.getUserId() == excludedData.getUserId()
						&& rating.getItemId() == excludedData.getItemId()) {
					userItemRatings.remove(rating);
					break;
				}
			}
		}
		return userItemRatings;
	}

	public HashMap<Long, List<UserItemRating>> getUserItemRatingsHash() {
		HashMap<Long, List<UserItemRating>> userItemHash = excludedData
				.getLoader().getUserItemRatingsHash();

		if (userItemRatingsHash == null) {
			userItemRatingsHash = new HashMap<Long, List<UserItemRating>>();
			for (Long userId : userItemHash.keySet()) {
				List<UserItemRating> uIRatings = userItemHash.get(userId);
				if (uIRatings != null) {
					List<UserItemRating> localUserItemRatings = new ArrayList<UserItemRating>();
					for (UserItemRating rating : uIRatings) {
						localUserItemRatings.add(rating);
					}
					userItemRatingsHash.put(userId, localUserItemRatings);
				}
			}

			List<UserItemRating> userItemList = userItemRatingsHash
					.get(excludedData.getUserId());
			if (null != userItemList) {
				for (UserItemRating rating : userItemList) {
					if (rating.getUserId() == excludedData.getUserId()
							&& rating.getItemId() == excludedData.getItemId()) {
						userItemList.remove(rating);
						break;
					}
				}
				userItemRatingsHash.put(excludedData.getUserId(), userItemList);
			}
		}
		return userItemRatingsHash;
	}

	public Set<Long> getUsers() {
		return excludedData.getLoader().getUsers();
	}

	public HashMap<Long, List<Long>> getUserItemHash() {

		if (localUserItemsHash == null) {
			localUserItemsHash = new HashMap<Long, List<Long>>();
			HashMap<Long, List<Long>> userItemHash = excludedData.getLoader()
					.getUserItemHash();
			for (Long userId : userItemHash.keySet()) {

				if (userItemHash.get(userId) != null) {

					List<Long> localItems = new ArrayList<Long>();
					for (Long item : userItemHash.get(userId)) {
						localItems.add(item);
					}
					localUserItemsHash.put(userId, localItems);
				}
			}
			List<Long> items = localUserItemsHash.get(excludedData.getUserId());
			if (null != items) {
				items.remove(excludedData.getItemId());
				localUserItemsHash.put(excludedData.getUserId(), items);
			}
		}
		return localUserItemsHash;

	}

	public HashMap<Long, List<Long>> getItemUserHash() {
		if (localItemUsersHash == null) {
			HashMap<Long, List<Long>> itemUserHash = excludedData.getLoader()
					.getItemUserHash();
			localItemUsersHash = new HashMap<Long, List<Long>>();
			for (Long itemId : itemUserHash.keySet()) {

				if (itemUserHash.get(itemId) != null) {

					List<Long> localUsers = new ArrayList<Long>();
					for (Long user : itemUserHash.get(itemId)) {
						localUsers.add(user);
					}
					localItemUsersHash.put(itemId, localUsers);
				}
			}
			List<Long> users = localItemUsersHash.get(excludedData.getItemId());
			if (null != users) {
				users.remove(excludedData.getUserId());
				localItemUsersHash.put(excludedData.getItemId(), users);
			}
		}

		return localItemUsersHash;
	}

	public List<UserTrustRating> getUserTrustRatings() {
		return excludedData.getLoader().getUserTrustRatings();
	}

	public Map<Long, List<UserTrustRating>> getUserTrustRatingsHash() {
		return excludedData.getLoader().getUserTrustRatingsHash();
	}

	public Map<Long, UserBo> getUserBoMap() {
		if (null == localUserBoMap) {
			Map<Long, UserBo> userBosMap = excludedData.getLoader()
					.getUserBoMap();
			localUserBoMap = new HashMap<Long, UserBo>();
			for (Long userId : userBosMap.keySet()) {
				UserBo user = userBosMap.get(userId);
				if (null != user) {
					localUserBoMap.put(userId, getClonedUser(user));
				}
			}
			UserBo user = localUserBoMap.get(excludedData.getUserId());
			List<Long> itemsRated = user.getItemsRated();
			if (itemsRated.contains(excludedData.getItemId())) {
				itemsRated.remove(excludedData.getItemId());
				user.setItemsRated(itemsRated);

			}
			Map<Long, Float> itemsRatingMap = user.getItemRatingsMap();
			if (itemsRatingMap.containsKey(excludedData.getItemId())) {
				itemsRatingMap.remove(excludedData.getItemId());
				user.setItemRatingsMap(itemsRatingMap);
			}
			localUserBoMap.put(excludedData.getUserId(), user);
		}

		return localUserBoMap;
	}

	private UserBo getClonedUser(UserBo user) {
		UserBo retVal = new UserBo();
		retVal.setUserId(user.getUserId());
		retVal.setTrustedUsers(user.getTrustedUsers());
		retVal.setUserTrustMap(user.getUserTrustMap());
		Map<Long, Float> lItemRatings = new HashMap<Long, Float>();

		Map<Long, Float> uItemRatings = user.getItemRatingsMap();
		if (null != uItemRatings) {
			for (Long item : uItemRatings.keySet()) {
				Float rating = uItemRatings.get(item);
				lItemRatings.put(item, rating);
			}
		}
		retVal.setItemRatingsMap(lItemRatings);
		List<Long> lItems = new ArrayList<Long>();
		List<Long> uItems = user.getItemsRated();
		for (Long itemId : uItems) {
			lItems.add(itemId);
		}
		retVal.setItemsRated(lItems);
		return retVal;
	}

}

package edu.trust;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

import edu.loader.DataLoader;
import edu.models.UserBo;
import edu.models.UserTrustRating;

/**
 * Given an user and an item, this algorithm calculates the trust ratings for 
 * the users that have rated the item using Golbeck's Tidal Trust algorithm.
 * It returns the users that has rated the item at the minimum possible 
 * depth from the user. 
 * 
 * Also the algorithm takes the max depth allowed and if there are no users 
 * found at the depth requested that has rated the item, returns empty.
 *  
 * 
 *
 */

public class TidalTrustProcessor {
	
	
	public static UserTrustResponse getTrustRatings (UserTrustRequest req){
		UserTrustResponse trustResp = new UserTrustResponse();
		try{
			trustResp.setRequestedUserId(req.getUserId());
			trustResp.setRequestedItemId(req.getItemId());
			Map<Long, Float> trustMap = new HashMap<Long, Float>();
			Set<Long> destUsers = new TreeSet<Long>();
			Map<Long, UserBo> userBoMap = req.getLoader().getUserBoMap();
			Map<Long, Boolean> nodeVisitedMap = new HashMap<Long, Boolean>();
			Map<UserBo, Float> userPathFlow =  new HashMap<UserBo, Float>();
			Map<Integer, List<UserBo>> usersAtDepthMap = new HashMap<Integer, List<UserBo>>();
		
			// Used during backtracking
			Map<UserBo, List<UserBo>> userChildren = new HashMap<UserBo, List<UserBo>>();
			Map<UserBo, List<DestinationRating>> userDestRating = new HashMap<UserBo, List<DestinationRating>>();
			
			int maxDepth = req.getMaxDepth();
			Queue<UserBo> toProcessQueue = new LinkedList<UserBo>(); // q in tidal trust
			UserBo userBo = userBoMap.get(req.getUserId());
			toProcessQueue.add(userBo);
			
			int currentDepth = 1;
			List<UserBo> nonVisitedUsersAtCurrentDepth = new ArrayList<UserBo>();
			while (!toProcessQueue.isEmpty() && currentDepth <= maxDepth){
				
				UserBo processUser = toProcessQueue.remove();
				// Add user to the users at current depth
				usersAtDepthMap = addUserToDepthMap(usersAtDepthMap, currentDepth, processUser);
				List<Long> trustedUsers = processUser.getTrustedUsers();
				//System.out.println("size is"+trustedUsers.size());
				boolean foundSink = false;
				for (Long userId : trustedUsers){
					
					UserBo tUser = userBoMap.get(userId);
					//This can happen if the trusted user didn't rate any items
					if (tUser == null){
						continue;
					}
					// If the user has rated the item
					if (tUser.getItemsRated().contains(req.getItemId())){
						foundSink = true;
						userDestRating = addDestRating(userDestRating, processUser, new DestinationRating(tUser, processUser.getUserTrustMap().get(tUser.getUserId())));					
						destUsers.add(tUser.getUserId());
						maxDepth = currentDepth;// Since sink is found, restrict max depth to current depth
						Float currentFlow = getMinimunFlow(userPathFlow, processUser, processUser.getUserTrustMap().get(tUser.getUserId()));
						Float tUserFlow = userPathFlow.get(tUser);
						if (tUserFlow != null){
							
							tUserFlow = Math.max(tUserFlow, currentFlow);
						} else {
							tUserFlow = currentFlow;
						}
						userPathFlow.put(tUser, tUserFlow);
						// Add child node
						addUserChildren(userChildren, processUser, tUser);
					} 
				}
				
				if (!foundSink){
					for (Long userId : trustedUsers){
						UserBo tUser = userBoMap.get(userId);
						if (tUser == null){
							continue;// This happens if the user doesn't rate any items and doesn't trust any users
						}
						if (nodeVisitedMap.get(tUser.getUserId()) == null || 
								!nodeVisitedMap.get(tUser.getUserId())){
							nodeVisitedMap.put(tUser.getUserId(), Boolean.TRUE);
							nonVisitedUsersAtCurrentDepth.add(tUser);
						}
						if (nonVisitedUsersAtCurrentDepth.contains(tUser)){
							Float currentFlow = getMinimunFlow(userPathFlow, processUser, processUser.getUserTrustMap().get(tUser.getUserId()));
							Float tUserFlow = userPathFlow.get(tUser);
							if (tUserFlow != null){
								
								tUserFlow = Math.max(tUserFlow, currentFlow);
							} else {
								tUserFlow = currentFlow;
							}
							userPathFlow.put(tUser, tUserFlow);
							// Add child node
							addUserChildren(userChildren, processUser, tUser);
						}
					}
				}
				if (toProcessQueue.isEmpty()){
					toProcessQueue.addAll(nonVisitedUsersAtCurrentDepth);
					nonVisitedUsersAtCurrentDepth= new ArrayList<UserBo>();
					currentDepth++;
				}
				
				
			}
			
			currentDepth--;
			
			for (Long destUserId : destUsers){ // Users that has a rating to the sink
				UserBo destUser = userBoMap.get(destUserId);
				float max = userPathFlow.get(destUser);
				int runningDepth = currentDepth;
				Map<UserBo, Float> ratingForDest = new HashMap<UserBo, Float>();
				boolean foundAtDepthOne = false;
				if (!usersAtDepthMap.get(runningDepth).isEmpty()){
					for (UserBo user: usersAtDepthMap.get(runningDepth)){
						if (user.getUserId() == userBo.getUserId() && user.getTrustedUsers().contains(destUserId)){
							foundAtDepthOne = true;
							if (user.getUserTrustMap().get(destUserId) >= max){
								trustMap.put(destUserId, user.getUserTrustMap().get(destUserId));
								break;
							}	
						}
					}
					;
				} 
				runningDepth--;
				if (!foundAtDepthOne){
					Map<Long, Float> cachedRatingToDestination = new HashMap<Long, Float>();
					for (UserBo user : userDestRating.keySet()){
						for (DestinationRating destRating : userDestRating.get(user)){
							if (destRating.destination.getUserId() ==  destUserId && 
									destRating.rating >=0F){
								cachedRatingToDestination.put(user.getUserId(), destRating.rating);
							}
						}
					}
					while (runningDepth >0){
						while (!usersAtDepthMap.get(runningDepth).isEmpty()){
							for (UserBo user: usersAtDepthMap.get(runningDepth)){
								Float numer = 0.0F;
								Float den = 0.0F;
								if (null != userChildren){
									for (UserBo childUser : userChildren.get(user)){
										if (user.getUserTrustMap().get(childUser.getUserId()) >= max && 
												cachedRatingToDestination.get(childUser.getUserId()) != null && 
												cachedRatingToDestination.get(childUser.getUserId()) >=0.0F){
											/*for (DestinationRating destRating : userDestRating.get(childUser)){
												if (destRating.destination.getUserId() ==  destUserId && 
														destRating.rating >=0F){
													numer+= user.getUserTrustMap().get(childUser.getUserId()) * destRating.rating;
													den+= user.getUserTrustMap().get(childUser.getUserId());
												}
											}*/
											numer+= user.getUserTrustMap().get(childUser.getUserId()) * cachedRatingToDestination.get(childUser.getUserId());
											den+= user.getUserTrustMap().get(childUser.getUserId());
											
										}
									}
								}	
								if (den > 0){
									ratingForDest.put(user, numer/den);
									cachedRatingToDestination.put(user.getUserId(), numer/den);
								} else {
									ratingForDest.put(user, -1.0F);
									cachedRatingToDestination.put(user.getUserId(), -1.0F);
								}
							}
						}
						runningDepth--;
					}
					trustMap.put(destUserId, ratingForDest.get(userBo));
				}	
			}
			trustResp.setUserTrustRatingsMap(trustMap);
		} catch (Exception e){
			e.printStackTrace();
		}
		return trustResp;
		
	}
	
	private static Map<UserBo, List<UserBo>> addUserChildren(Map<UserBo, List<UserBo>> userChildren, UserBo parent, UserBo child){
		
		List<UserBo> users =  userChildren.get(parent);
		if (null == users){
			users = new ArrayList<UserBo>();
		}
		users.add(child);
		userChildren.put(parent, users);
		return userChildren;
	}
	
	
	private static Map<Integer, List<UserBo>> addUserToDepthMap(Map<Integer, List<UserBo>> usersAtDepthMap, int depth, UserBo user){
		
		List<UserBo> users =  usersAtDepthMap.get(depth);
		if (null == users){
			users = new ArrayList<UserBo>();
		}
		users.add(user);
		usersAtDepthMap.put(depth, users);
		return usersAtDepthMap;
	}
	
	private static Float getMinimunFlow(Map<UserBo, Float> userPathFlow, UserBo user, Float rating){
		Float userPathMin = userPathFlow.get(user);
		if (null == userPathMin){
			return rating;
		}
		return Math.min(userPathMin,rating);
	}
	
	/**
	 * Adds the destination rating from source user to dest user
	 * @param userDestRating
	 * @param user
	 * @param dest
	 * @return
	 */
	private static Map<UserBo, List<DestinationRating>> addDestRating (Map<UserBo, List<DestinationRating>> userDestRating, UserBo user, DestinationRating dest){
		List<DestinationRating> ratings = userDestRating.get(user);
		if (null == ratings){
			ratings = new ArrayList<DestinationRating>();
			
		}
		ratings.add(dest);
		userDestRating.put(user,ratings);
		return userDestRating;
	}
	
	
	public static class DestinationRating{
		
		public UserBo destination;
		public Float rating;
		
		public DestinationRating(UserBo dest, Float rating){
			this.destination = dest;
			this.rating = rating;
					
		}
	}

}

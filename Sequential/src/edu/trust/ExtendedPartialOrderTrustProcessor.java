package edu.trust;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import edu.dal.DBConnectionManager;
import edu.loader.DataLoader;
import edu.models.UserBo;
import edu.models.UserItemRating;

public class ExtendedPartialOrderTrustProcessor {
	
	public static UserTrustResponse getTrustRatings (UserTrustRequest req){
		UserTrustResponse trustResp = new UserTrustResponse();
		
		try{
			
			trustResp.setRequestedUserId(req.getUserId());
			trustResp.setRequestedItemId(req.getItemId());
			
			Map<Long, Float> usersToProcess = new HashMap<Long, Float>();
			usersToProcess.put(req.getUserId(), null);
			int depth = 1;
			Set<Long> excludedList = new TreeSet<Long>();
			getPartialOrderTrust(req, usersToProcess, excludedList, depth, trustResp);
			
			// Get the most trusted users of a node
		} catch (Exception e){
			e.printStackTrace();
		}
			
		return trustResp;
	}
	
	
	private static void getPartialOrderTrust(UserTrustRequest req, Map<Long, Float> usersToProcess, Set<Long> excludedUsers, int depth , UserTrustResponse resp){
			
			try{
					
					// get the max depth
					int maxDepth = req.getMaxDepth();
					// get trust threshold
					float trustThreshold = req.getMinThresholdForTrust();
					
					DataLoader loader = req.getLoader();
					
					long itemId = req.getItemId();
					
					
					boolean foundSink = false;
					
					//Pre-Condition
					if (usersToProcess == null || usersToProcess.isEmpty()){
						return;
					}
					Map<Long, Float> usersToProcessInNextCycle = new HashMap<Long, Float>();
					//Set<MinTrustUserInfo> usersToProcessInNextCycle = new TreeSet<MinTrustUserInfo>();
					
					
					// Check if sink user is present in current trusted nodes
					// Initially the source will be in usersToProcess. UsersToProcessInNextCycle will be 
					// empty. 
					for (Long userId : usersToProcess.keySet()){
						UserBo user = loader.getUserBoMap().get(userId);
						if (null == user){
							break; // User is not present in user trust map
						}
						Map<Long, Float> userTrustMap = user.getUserTrustMap();
						
						if (null ==  userTrustMap){
							break;
						}
						boolean foundcurrentSink = false;
						Long currentUserId = 0L;
						Map<Long, UserItemRating> currentProcessedUsers = new HashMap<Long, UserItemRating>();//for modified partial order algorithm for optimization
						for (Long trustUserId : userTrustMap.keySet()){
							UserBo tUser = loader.getUserBoMap().get(trustUserId);
							if (null != tUser){
								UserItemRating userItemRating = DBConnectionManager.getUserItemRatingByUserIdItemId(trustUserId, itemId);
								currentProcessedUsers.put(trustUserId, userItemRating);
								if (userItemRating != null && userItemRating.getItemId()>0L && user.getUserTrustMap().get(trustUserId)>0F){// Found sink
									Map<Long, Float> userTrustRatingsMap = resp.getUserTrustRatingsMap();
									if (userTrustRatingsMap == null){
										userTrustRatingsMap  = new HashMap<Long, Float>();
										resp.setDepthFound(depth);
									} 
									// Found user that rated the item
									
									userTrustRatingsMap.put(trustUserId, user.getUserTrustMap().get(trustUserId));
									resp.setUserTrustRatingsMap(userTrustRatingsMap);
									foundSink = true;
									foundcurrentSink = true;// what is the significance of this flag
									currentUserId = trustUserId;
									break;
								}	
							}	
						}
						// Add other users also irrespective of trust value for modified partial order algorithm
						if (foundcurrentSink){
							for (Long trustUserId : userTrustMap.keySet()){
								if (trustUserId.longValue() == currentUserId.longValue()){// Ignore as this is for modified partial order algorithm
									continue;
								}
								UserBo tUser = loader.getUserBoMap().get(trustUserId);
								if (null != tUser){
									UserItemRating userItemRating = null;
									if (currentProcessedUsers.containsKey(trustUserId)){
										 userItemRating = currentProcessedUsers.get(trustUserId);
									} else {
									 userItemRating = DBConnectionManager.getUserItemRatingByUserIdItemId(trustUserId, itemId);
									} 
									if (userItemRating != null && userItemRating.getItemId()>0L ){// Found sink
										Map<Long, Float> userTrustRatingsMap = resp.getUserTrustRatingsMap();
										
										// Found user that rated the item
										
										userTrustRatingsMap.put(trustUserId, user.getUserTrustMap().get(trustUserId));
										resp.setUserTrustRatingsMap(userTrustRatingsMap);
										
									}	
								}	
							}
						}
						
					}
					
					if (foundSink){
						return;
					}
					
					for (Long userId : usersToProcess.keySet()){
						
						Float currTrust = usersToProcess.get(userId);
						//UserBo user = loader.getUserBoMap().get(userId);
						Map<Float, List<Long>> priorityTrustUserMap = getMostTrustedUsers(userId, loader, trustThreshold, excludedUsers);
						//excludedUsers.addAll(user.getTrustedUsers());
						if (null != priorityTrustUserMap && !priorityTrustUserMap.isEmpty()){
							Iterator<Float> priorityIter = priorityTrustUserMap.keySet().iterator();
							// Currently we getting only the most trusted neighbors. 
							// This works for epinions data set since it's a binary trust ratings.
							// For movielens, need to modifiy to get first n trusted neighbors.
							Float trust = priorityIter.next();
							
							List<Long> trustedUsers = priorityTrustUserMap.get(trust);
							if (null != trustedUsers){
								for (Long tUserId : trustedUsers){
									UserBo tUser = loader.getUserBoMap().get(tUserId);
									if (tUser == null){
										continue;
									}
									// Set the min trust via the path
									if (currTrust == null || currTrust > trust){
										currTrust = trust;
									} 
									
									// populate users to process in next cycle
									Float mapRating = usersToProcessInNextCycle.get(tUserId);
									if (mapRating == null || mapRating < currTrust){ // Keep the max trust if two or more nodes rate the same user.
										usersToProcessInNextCycle.put(tUserId, currTrust);
									}
								}
							}
						}
					}
					
					
					
					
					// Check if most trusted users have sink node
					
					/*if (null != usersToProcessInNextCycle){
						for (Long userToProcess: usersToProcessInNextCycle.keySet()){
							UserBo tUser = loader.getUserBoMap().get(userToProcess);
							UserItemRating userItemRating = DBConnectionManager.getUserItemRatingByUserIdItemId(userToProcess, itemId);
							if (userItemRating != null && userItemRating.getItemId()>0L){// Found sink
								Map<Long, Float> userTrustRatingsMap = resp.getUserTrustRatingsMap();
								if (userTrustRatingsMap == null){
									userTrustRatingsMap  = new HashMap<Long, Float>();
									resp.setDepthFound(depth);
								} 
								// Found user that rated the item
								
								userTrustRatingsMap.put(userToProcess, usersToProcessInNextCycle.get(userToProcess));
								resp.setUserTrustRatingsMap(userTrustRatingsMap);
								foundSink = true;
							} 
						}
					}*/	
					
					
					if (foundSink){
						return;
					} else {
						depth++;
						//if (!foundSink || depth <= maxDepth){
						if (depth <= maxDepth){
							
							// Filter usersToProcessInNextCycle based on count based semantics
							
							if (null != usersToProcessInNextCycle){
								for (Long userToProcess: usersToProcessInNextCycle.keySet()){
									int trustCount = 0;
									for (Long userId : usersToProcess.keySet()){
										if (!excludedUsers.contains(userId)){ // Consider this if excluded users doesn't contain the userId already
											UserBo user = loader.getUserBoMap().get(userId);
											if (user.getUserTrustMap().get(userToProcess) != null){ 
												if (user.getUserTrustMap().get(userToProcess) >0){ // Greater than 0 is treated as trust
													trustCount++;
												} else {
													trustCount--;
												}
											}
										}	
									}
									if (trustCount <=0){
										// Remove
										usersToProcessInNextCycle.remove(userToProcess);
									}
								}
							}
							
							
							
							// Populate Excluded Users
							for (Long userId : usersToProcess.keySet()){
								
								UserBo user = loader.getUserBoMap().get(userId);
								if (user != null){
									excludedUsers.addAll(user.getTrustedUsers());
								}	
							}
							// Get the list of next users to process
							
							// Call recursive function
							getPartialOrderTrust(req,usersToProcessInNextCycle,excludedUsers, depth, resp  );
							
						}
						
					}
			} catch (Exception e){
				e.printStackTrace();
			}
	}
	
	/**
	 * Get trusted users in decreasing order of Trust Rating.
	 * @param userId
	 * @param loader
	 * @param trustThreshold
	 * @param excludedList
	 * @return
	 */
	private static Map<Float, List<Long>> getMostTrustedUsers(Long userId, DataLoader loader, Float trustThreshold, Set<Long> excludedList){
		
		UserBo user = loader.getUserBoMap().get(userId);
		if (user == null){
			// User not present in trust table
			return null;
		} /*else {
			System.out.println("Found User");
		}*/
		
		Map<Float, List<Long>> retVal = new LinkedHashMap<Float, List<Long>>();
		
		// Get the most trusted users
		
		Map<Long, Float> sortedByTrust = sortMapByDescendingValue(user.getUserTrustMap());
		
		for (Long tUserId : sortedByTrust.keySet()){
			Float tRating = sortedByTrust.get(tUserId);
			if (!excludedList.contains(tUserId)){
				if (tRating >= trustThreshold){
					List<Long> trustedUsers = retVal.get(tRating);
					if (trustedUsers != null){
						
						trustedUsers.add(tUserId);
					} else {
						trustedUsers = new ArrayList<Long>();
						trustedUsers.add(tUserId);
						
					}
					retVal.put(tRating, trustedUsers);
				} else {
					// Trust rating is less than threshold
					return retVal;
				}
			}	
		}
	
		return retVal;
			
		
		
	}
	
	
	/**
	 * Sorts the Map by value in decreasing order of values.
	 * @param map
	 * @return
	 */
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static  <K,V extends Comparable>  Map<K,V> sortMapByDescendingValue(Map<K,V> map){     



        List<Entry<K,V>> listForSort = null;
        
        // Create linked hash map so that order of entries is preserved.
        Map<K,V>  valueSortMap= new LinkedHashMap<K,V>();     

        listForSort = new LinkedList<Entry<K,V>>(map.entrySet());

        Collections.sort(listForSort, new Comparator<Entry<K,V>>() {

             
			public int compare(Entry<K,V> value1,Entry<K,V> value2) {

            	 return value2.getValue().compareTo(value1.getValue());

             }

        });

       Iterator<Entry<K,V>> itret =listForSort.iterator();



        while(itret.hasNext()){

             Entry<K,V> entry = itret.next();

             valueSortMap.put(entry.getKey(), entry.getValue());

       } 

        return valueSortMap;

  }
	
	
	static class MinTrustUserInfo{
		public Long userId;
		public Float minTrust;
		
		public MinTrustUserInfo(long userId, Float minTrust){
			this.userId = userId;
			this.minTrust = minTrust;
		}
		
		@Override
		public int hashCode(){
			return userId.hashCode();
		}
	}
	
	static class UserTrustInCurrentCycle{
		public int trustCount = 0;
		public Float minTrust;
		
		public UserTrustInCurrentCycle(Float minTrust){
			this.minTrust = minTrust;
		}
		public UserTrustInCurrentCycle(){
			
		}
		
		
	}
	

}

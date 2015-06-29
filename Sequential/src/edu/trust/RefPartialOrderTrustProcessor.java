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
import java.util.TreeSet;

import edu.loader.DataLoader;
import edu.models.UserBo;

/**
 * 
 * 
 * 
 * 1. Get the most trusted neighbors of the user in order of priority. (Trust threshold controls the threshold value for trust.
 * 2. Assign trust for the user as the min of the trust for the parent user and trust value from parent user to the user.
 * 3. At the same depth, consider the max depth for the user i.e., if two parent users rate the same user at 
 *    same depth then consider the max depth.
 * 
 * 
 * 1 for each n in G
 * 2 	color(n) = white
 * 3 	q = empty
 * 		count(sink) = infinity	
 * 4 PartialOrderTrust(source, sink)
 * 5	push(q, source)
 * 6	while q not empty 
 * 7		node = pop(q)		
 * 9		if sink in adj(node)
 * 				found(sink) = true
 * 10			if (trust(node) // Within same context
 * 11				count(sink) +=1
 * 12				rating(sink) = max(rating(sink), rating(node, sink)
 * 13			else 
 * 14				count(sink) -=1
 * 			else 
 * 				for each n2 in adj(node)
 * 					if color(n2) = white
 * 						color(n2)= grey
 * 						push(temp_q, n2) // Make sure nodes at the same depth are considered
 * 					if n2 in temp_q and most_trusted(node) has n2 // Within same context
 * 						push(most_trusted_q,n2)
 * 						if (trust(node,n2) 
 * 11						count(n2) +=1
 * 12						rating(n2) = max(rating(n2), rating(node, n2)
 * 13					else 
 * 14						count(n2) -=1
 * 								
 * 15		if (found(sink)
 * 16			if (count(sink) >0)
 * 17				return TRUST, rating(sink)
 * 18			else if (count(sink) = 0)
 * 19				return AMBIGUOUS
 * 20			else if (count(sink) < 0)
 * 21				return DISTRUST
 * 22			
 * 24.		else 
 * 				q = most_trusted_q 
 * 				most_trusted_q = empty
 * 				temp_q = empty
 * 			
 * 		return NO_INFO		
 *
 

1 for each n in G
2 	color(n) = white
3 	q = empty
4 PartialOrderTrust(source, sink)
5	push(q, source)
6	while q not empty 
7		node = pop(q)		
8		if sink in adj(node)
9 			found(sink) = true
10			if (trust(node) // Within same context
11				count(sink) +=1
12				rating(sink) = max(rating(sink), rating(node, sink)
13			else 
14				count(sink) -=1
15 		else 
16 			for each n2 in adj(node)
17 				if color(n2) = white
18 					color(n2)= grey
19 					push(temp_q, n2) // Make sure nodes at the same depth are considered
20 				if n2 in temp_q and most_trusted(node) has n2 // Within same context
21 					push(most_trusted_q,n2)
22					if (trust(node,n2) 
23						count(n2) +=1
24						rating(n2) = max(rating(n2), rating(node, n2)
25					else 
26						count(n2) -=1
 								
27		if (found(sink)
28			if (count(sink) >0)
29				return TRUST, rating(sink)
30			else if (count(sink) = 0)
31				return AMBIGUOUS
32			else if (count(sink) < 0)
33				return DISTRUST
			
34		else 
35			q = most_trusted_q 
36 			most_trusted_q = empty
37 			temp_q = empty
 			
38 	return NO_INFO
*/

public class RefPartialOrderTrustProcessor {
	
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
					for (Long userId : usersToProcess.keySet()){
						UserBo user = loader.getUserBoMap().get(userId);
						if (null == user){
							break; // User is not present in user trust map
						}
						Map<Long, Float> userTrustMap = user.getUserTrustMap();
						if (null ==  userTrustMap){
							break;
						}
						for (Long trustUserId : userTrustMap.keySet()){
							UserBo tUser = loader.getUserBoMap().get(trustUserId);
							if (null != tUser){
								if (tUser.getItemRatingsMap().containsKey(itemId)){
									Map<Long, Float> userTrustRatingsMap = resp.getUserTrustRatingsMap();
									if (userTrustRatingsMap == null){
										userTrustRatingsMap  = new HashMap<Long, Float>();
										resp.setDepthFound(depth);
									} 
									// Found user that rated the item
									
									userTrustRatingsMap.put(trustUserId, user.getUserTrustMap().get(trustUserId));
									resp.setUserTrustRatingsMap(userTrustRatingsMap);
									foundSink = true;
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
							//Currently we getting only the most trusted neighbors. 
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
					
					// Filter usersToProcessInNextCycle based on count based semantics
					if (null != usersToProcessInNextCycle){
						for (Long userToProcess: usersToProcessInNextCycle.keySet()){
							int trustCount = 0;
							for (Long userId : usersToProcess.keySet()){
								
								UserBo user = loader.getUserBoMap().get(userId);
								if (user.getUserTrustMap().get(userToProcess) != null){ 
									if (user.getUserTrustMap().get(userToProcess) >0){ // Greater than 0 is treated as trust
										trustCount++;
									} else {
										trustCount--;
									}
								}
							}
							if (trustCount <=0){
								// Remove
								usersToProcessInNextCycle.remove(userToProcess);
							}
						}
					}
					
					
					// Check if most trusted users have sink node
					
					/*if (null != usersToProcessInNextCycle){
						for (Long userToProcess: usersToProcessInNextCycle.keySet()){
							UserBo tUser = loader.getUserBoMap().get(userToProcess);
							if (tUser.getItemRatingsMap().containsKey(itemId)){
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
					}	*/
					
					
					if (foundSink){
						return;
					} else {
						depth++;
						while (foundSink || depth <= maxDepth){
							// Populate Excluded Users
							for (Long userId : usersToProcess.keySet()){
								
								UserBo user = loader.getUserBoMap().get(userId);
								excludedUsers.addAll(user.getTrustedUsers());
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

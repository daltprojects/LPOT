package edu.trust;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

import edu.dal.DBConnectionManager;
import edu.loader.DataLoader;
import edu.models.UserBo;
import edu.models.UserItemRating;
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
 * for each n in G color(n) = white q = empty
4 TidalTrust (source, sink) 5 push (q, source)
6 depth=1
7   maxdepth = infinity
8 while q not empty and depth ² maxdepth
9 n = pop(q)
10     push (d(depth), n)
11 if sink in adj(source)
12        cached_rating(n,sink) = rating(n,sink)
13        maxdepth = depth
14        flow = min(path_flow(n), rating(n,sink))
15        path_flow(sink) = max (path_flow(sink), flow)
16        push (children(n), sink)
17 else
18 for each n2 in adj(n)
19 if color(n2) = gray
20             color(n2) = gray
21             push (temp_q, n2)
22 if n2 in temp_q
23             flow = min(path_flow(n), rating(n,n2))
24             path_flow(n2) = max (path_flow(n2), flow)
25             push (children(n), n2)
26 if q empty
27 q = temp_q
28        depth = depth +1
29        temp_q = empty
30  max = path_flow(sink)
31  depth = depth-1
32 while depth>0
33 34 35 36 37
while d(depth) not empty
n = pop(d(depth))
for each n2 in children(n)
if (rating(n,n2)>=max) and cached_rating(n2,sink)³0 numerator =
numerator + rating(n,n2)* cached_rating(n2,sink) denominator = denominator +rating(n,n2)
if denominator > 0
cached_rating(n,sink) = numerator / denominator
else
38
39
40
41
42
43
44 return cached_rating(source, sink)
 *  
 * 
 * 
 * For the case of epinions data set since the sink is the first encountered user that rated the item, we will
 * determine the users with lowest depth from source that rated the item. Then source item trust is taken as the 
 * weighted average or the max average.
 *
 */

public class RefTidalTrustProcessor {
	
	private static Map<Long, UserBo> userBoMap = null;
	
	
	// Keeps track of whether a node has been already visited (TODO: Check if it can be derived)
	private static Map<Long, Boolean> nodeVisitedMap = new HashMap<Long, Boolean>();
	
	// Keeps track of the current flow (max strength weight from source to this user) for this user. 
	private static Map<UserBo, Float> userPathFlow =  new HashMap<UserBo, Float>();
	
	// Map having the users processed at current depth
	private static Map<Integer, List<UserBo>> usersAtDepthMap = new HashMap<Integer, List<UserBo>>();
	
	// Data structures used during backtracking
	
	// Children of the current user that is considered for trust calculation
	private static Map<UserBo, List<UserBo>> userChildren = new HashMap<UserBo, List<UserBo>>();
	
	// Keeps track of rating of destination users from the user
	private static Map<UserBo, List<DestinationRating>> userDestRating = new HashMap<UserBo, List<DestinationRating>>();
	
	
	private static int depthFound = 0;
	
	
	public static UserTrustResponse getTrustRatings (UserTrustRequest req){
		UserTrustResponse trustResp = new UserTrustResponse();
		try{
			// INitialize UserBo Map
			userBoMap = req.getLoader().getUserBoMap();
			
			Long itemId = req.getItemId();
			Long srcUserId = req.getUserId();
			int maxDepth = req.getMaxDepth();
			trustResp.setRequestedUserId(req.getUserId());
			trustResp.setRequestedItemId(req.getItemId());
			
			// Sink users and associated trust ratings to the source
			Map<Long, Float> trustMap = new HashMap<Long, Float>();
			
			// Sink users
			Set<Long> destUsers = new TreeSet<Long>();
			
			UserBo source = getSourceUserBo(req, srcUserId);
			
			trustMap = getTidalTrustRatingsForUser (source,itemId, maxDepth);
			trustResp.setUserTrustRatingsMap(trustMap);
			trustResp.setDepthFound(depthFound);
		} catch (Exception e){
			e.printStackTrace();
		}
		return trustResp;
			
	}		
	
	
	
	
	public static Map<Long, Float> getTidalTrustRatingsForUser (UserBo srcUser, Long itemId, int maxDepth) throws Exception{
		
		// Sink users and associated trust ratings to the source
		Map<Long, Float> srcToSinkUsertrustMap = new HashMap<Long, Float>();
		
		// Initialize data structures for Tidal Trust Algorithm.
		

		
		// Queue to process users at each depth of the graph
		Queue<UserBo> toProcessQueue = new LinkedList<UserBo>();
		
		Set<Long> destUsers = new TreeSet<Long>();
		
		toProcessQueue.add(srcUser);
		
		int currentDepth = 1;
		List<UserBo> nonVisitedUsersAtCurrentDepth = new ArrayList<UserBo>();
		
		while (!toProcessQueue.isEmpty() && currentDepth <= maxDepth){
			
			UserBo processUser = toProcessQueue.remove();
			if (processUser == null){//If user doesn't have trust ratings
				continue;
			}
			// Add user to the users at current depth
			addUserToDepthMap(currentDepth, processUser);
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
				//if (tUser.getItemsRated().contains(itemId)){
				UserItemRating userItemRating = DBConnectionManager.getUserItemRatingByUserIdItemId(userId, itemId);
				if (userItemRating != null && userItemRating.getItemId()>0L && processUser.getUserTrustMap().get(userId)>0F){// Found sink	
					foundSink = true;
					depthFound = currentDepth;
				    addDestRating(processUser, new DestinationRating(tUser, getTrustRating (processUser, tUser)));					
					destUsers.add(tUser.getUserId());
					maxDepth = currentDepth;// Since sink is found, restrict max depth to current depth
					Float currentFlow = getMinimunFlow(processUser, getTrustRating (processUser, tUser));
					Float tUserFlow = userPathFlow.get(tUser);
					if (tUserFlow != null){
						
						tUserFlow = Math.max(tUserFlow, currentFlow);
					} else {
						tUserFlow = currentFlow;
					}
					userPathFlow.put(tUser, tUserFlow);
					// Add child node
					addUserChildren(processUser, tUser);
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
						Float currentFlow = getMinimunFlow(processUser, getTrustRating (processUser, tUser));
						Float tUserFlow = userPathFlow.get(tUser);
						if (tUserFlow != null){
							
							tUserFlow = Math.max(tUserFlow, currentFlow);
						} else {
							tUserFlow = currentFlow;
						}
						userPathFlow.put(tUser, tUserFlow);
						// Add child node
						addUserChildren(processUser, tUser);
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
		
		// Backtracking
		
		for (Long destUserId : destUsers){ // Users that has a rating to the sink
			UserBo destUser = userBoMap.get(destUserId);
			float max = userPathFlow.get(destUser);
			int runningDepth = currentDepth;
			Map<UserBo, Float> ratingForDest = new HashMap<UserBo, Float>();
			boolean foundAtDepthOne = false;
			
			// Case if sink is found at depth 1
			if (!usersAtDepthMap.get(runningDepth).isEmpty()){
				for (UserBo user: usersAtDepthMap.get(runningDepth)){
					if (user.getUserId() == srcUser.getUserId() && user.getTrustedUsers().contains(destUserId)){
						foundAtDepthOne = true;
						if (user.getUserTrustMap().get(destUserId) >= max){
							srcToSinkUsertrustMap.put(destUserId, user.getUserTrustMap().get(destUserId));
							break;
						}	
					}
				}
				;
			} 
			runningDepth--;
			
			// Not found at depth 1
			if (!foundAtDepthOne){
				Map<Long, Float> cachedRatingToDestination = new HashMap<Long, Float>();
				if (userDestRating.size() >0){
					// Updates the cache of users that rated destUserId
					for (UserBo user : userDestRating.keySet()){
						for (DestinationRating destRating : userDestRating.get(user)){
							if (destRating.destination.getUserId() ==  destUserId && 
									destRating.rating >=0F){
								cachedRatingToDestination.put(user.getUserId(), destRating.rating);
							}
						}
					}
					
					while (runningDepth >0){
						if (!usersAtDepthMap.get(runningDepth).isEmpty()){
							for (UserBo user: usersAtDepthMap.get(runningDepth)){
								Float numer = 0.0F;
								Float den = 0.0F;
								if (null != userChildren && userChildren.get(user) != null){
									for (UserBo childUser : userChildren.get(user)){
										if (getTrustRating(user, childUser) >= max && 
												cachedRatingToDestination.get(childUser.getUserId()) != null && 
												cachedRatingToDestination.get(childUser.getUserId()) >=0.0F){
											
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
					srcToSinkUsertrustMap.put(destUserId, ratingForDest.get(srcUser));
				}
			}	
		}
		
		return 	srcToSinkUsertrustMap;
		
	}
	
	
		
	private static Float getTrustRating (UserBo source, UserBo sink){
		
		return source.getUserTrustMap().get(sink.getUserId());
	}
		
	
	private static UserBo getSourceUserBo (UserTrustRequest req, Long userId){
		// UserBo for the source
		Map<Long, UserBo> userBoMap = req.getLoader().getUserBoMap();
		return userBoMap.get(req.getUserId());
	}
	
	
	private static void addUserChildren(UserBo parent, UserBo child){
		
		List<UserBo> users =  userChildren.get(parent);
		if (null == users){
			users = new ArrayList<UserBo>();
		}
		users.add(child);
		userChildren.put(parent, users);
		
	}
	
	
	private static void addUserToDepthMap(int depth, UserBo user){
		
		List<UserBo> users =  usersAtDepthMap.get(depth);
		if (null == users){
			users = new ArrayList<UserBo>();
		}
		users.add(user);
		usersAtDepthMap.put(depth, users);
		
	}
	
	private static Float getMinimunFlow(UserBo user, Float rating){
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
	private static void addDestRating (UserBo user, DestinationRating dest){
		List<DestinationRating> ratings = userDestRating.get(user);
		if (null == ratings){
			ratings = new ArrayList<DestinationRating>();
			
		}
		ratings.add(dest);
		userDestRating.put(user,ratings);
		
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

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.giraph.examples;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.giraph.edge.Edge;
import org.apache.giraph.graph.BasicComputation;
import org.apache.giraph.graph.Vertex;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.log4j.Logger;

/**
 * Implementation for Local Partial Order Trust Computation.
 */
@Algorithm(
    name = "Local Partial Order Trust",
    description = "Computes the trust for users in trust network. Depth is proportional to number of supersteps"
)
public class LocalPartialOrderTrustVertex extends
	BasicComputation<LongWritable, Text, DoubleWritable, Text> { 
  /** The shortest paths id */
  public static final String SOURCE_ID = "LocalPartialOrderTrust.sourceId";
  /** Default shortest paths id */
  public static final long SOURCE_ID_DEFAULT = 1;
  /** Class logger */
  private static final Logger LOG =
      Logger.getLogger(LocalPartialOrderTrustVertex.class);
  
  private static final String EXCLUDEDSTART = "ES";
  private static final String EXCLUDEDEND = "EN";
  private static final String DEPTHSTART = "DS";
  private static final String DEPTHEND = "DE";
  
  

  
  private Map<Integer, Map<Long, Double>> depthSourceRatingMap; 
  private Map<Long, Set<Long>> excludedUsersMap;
  
  static class CountValue{
	  public CountValue(){
		  this.count =0;
		  this.rating=0.0;
	  }
	  public Double rating;
	  public Integer count;
  }
  
  static class SourceRating{
	  public Long sourceId;
	  public Double rating;
	  
  }

  @Override
  public void compute(Vertex<LongWritable, Text, DoubleWritable> vertex, Iterable<Text> messages) {
	  
	  
	  int maxSuperSteps = getConf().getMaxNumberOfSupersteps();
	  depthSourceRatingMap = new HashMap<Integer, Map<Long, Double>>();
	  excludedUsersMap = new TreeMap<Long, Set<Long>>();
	  /**
	   * globalDepthSourceRatingMap = initialize [depth, [source:rating]];
	   * globalExcludedUsers = initialize [source: Set(excludedUsers)];
	   * 
	   * if (getSuperstep == 0){ // Initialize Phase executed during superstep 0 		
	   * 		excludedUsers = getAllEdgeVertices();
	   * 		depth = getSuperstep()+1;
	   * 		for (Edge edge : getEdges()){
	   * 			source=getVertexId();
	   * 			addToMessage(depth, [source:edge.getValue()])); 	    			
	   * 			addToMessage(source,excludedUsers); // To consider direct trusted neighbors
	   * 			sendMessage(edge.getTargetVertexId());
	   * 		}
	   * 		voteToHalt();
	   * 		return;
	   * }	
	   * // Process the incoming messages
	   * sourceCountMap = initialize [source:count];
	   * sourceRatingMap = initialize [source:rating];
	   * currentDepth = currentSuperStep;
	   * for (Message msg: getMessages()){
	   * 	// Consider source ratings at depth = current superstep
	   * 	Map sourceRatings =  getSourceRatingAtDepth(msg, getSuperStep());
	   * 	for each source in sourceRatings {
	   * 		if (trust(source)){
	   * 			increment count in sourceCountMap;
	   * 			if (sourceRating > rating from sourceRatingMap){
	   * 				sourceRatingMap.put(source, sourceRating);
	   * 		} else {
	   * 			decrement count in sourceCountMap;
	   * 		}
	   * 		excludedUsersForSource = getExcludedUsers (source, message);
	   * 		addToGlobalExcludedUsers (excludedUsersForSource);
	   * 	}		
	   * 
	   * }
	   * 
	   * for each source in sourceCountMap{
	   * 	if (count(source)>0){
	   * 		globalDepthSourceRatingMap.put (currentDepth, source:rating(source, sourceRatingMap)); 
	   * 	else if (count(source)<0){
	   * 		globalDepthSourceRatingMap.put (currentDepth, source:Distrusts); 
	   * 	} else if (count(source) == 0){
	   * 		globalDepthSourceRatingMap.put (currentDepth, source:AMBIGUOUS); 
	   * 	}
	   * }
	   * 
	   * if (getSuperStep() == conf.maxSuperStep()){
	   * 	voteToHalt();
	   * 	return;
	   * }
	   * 
	   * sourceInfoToSend = getGlobalSourceRatingAtDepth (currentDepth); // Get the source ratings at current depth
	   * depthToSend = currentDepth+1;
	   * for each edge in vertex.getEdges {
	   * 	for each source in sourceInfoToSend {
	   * 		if (targetVertex (edge) in excludedUsers(source)){
	   * 			continue;
	   * 		}
	   * 		if (trust(source){
	   * 			ratingToSend = min (rating(edge), currentRating(source));
	   * 			addToMessage(depthToSend, [source:ratingToSend])); 	    			
	   * 			addToMessage(source,globalExcludedUsers(source)+vertex.adjacentVertexIds);
	   * 			sendMessage(edge.getTargetVertexId());
	   * 		}
	   * 	}
	   * }
	   * voteToHalt();
	   * 	
	   * 
	   * 
	   * 
	   * 
	   * 				
	   * 100	0.0	101	1.0	102	1.0	103	0.0	104	1.0
101	0.0	105	1.0
102	0.0	105	0.0
103	0.0	105	1.0
104	0.0	102	1.0



103
101     1#100:1.0;
102     1#100:1.0,104:1.0;
100     1#100:1.0,104:1.0; (Incorrect)
105     1#103:1.0,100:1.0,101:1.0,104:1.0; (Incorrect)
104     1#103:1.0,100:1.0,101:1.0,104:1.0; (Incorrect)



Superstep:1;VertexId:103;Vertex value is: (Why excluded users are not sent)
Superstep:1;VertexId:101;Vertex value is:DS1:100,1.0#;DEES100:101,102,103,104,105,;EN (Why 105 is present in excluded users)
Superstep:1;VertexId:102;Vertex value is:DS1:100,1.0#;1:104,1.0#;DEES100:101,102,103,104,105,;104:102,105,;EN
Superstep:1;VertexId:100;Vertex value is:DS1:100,1.0#;1:104,1.0#;DEES100:101,102,103,104,105,;104:101,102,103,104,105,;EN
Superstep:1;VertexId:105;Vertex value is:DS1:103,1.0#;1:100,1.0#;1:101,1.0#;1:104,1.0#;DEES100:101,102,103,104,105,;101:105,;103:105,;104:101,102,103,104,105,;EN
Superstep:1;VertexId:104;Vertex value is:DS1:103,1.0#;1:100,1.0#;1:101,1.0#;1:104,1.0#;DEES100:101,102,103,104,105,;101:102,105,;103:102,105,;104:101,102,103,104,105,;EN
	   * 
	   * 
	   * 
	   * 
	   */
	  try{
		  LOG.debug("Start of super step:"+getSuperstep()+": for vertex"+vertex.getId());
		  System.out.println("Start of super step:"+getSuperstep()+": for vertex"+vertex.getId());
		 /**
		  * Condition to terminate
		  * If there are no out-bound edges from current vertex then it can vote for halt
		  */
		 if (getSuperstep()== maxSuperSteps+1){
			 vertex.voteToHalt();
			 return;
		 }
		  
		// System.out.println("Vertex value at startof super step:"+getSuperstep()+": for vertex"+vertex.getId()+":vertex value:"+vertex.getValue());
		 
		 /**
		  * Get all the outbound edges from current vertex and load them into
		  * LongArrayWritable. This can be used later to load excluded users and s
		  * send in the message to corresponding edges.
		  */
		 
		 Set<Long> excludedUsers = new TreeSet<Long>();
		 for (Edge<LongWritable, DoubleWritable> edge : vertex.getEdges()) {
			 // Populate excluded users
			 excludedUsers.add(edge.getTargetVertexId().get());
		 } 
		 excludedUsers.add(vertex.getId().get()); // Add current vertex as well
		 StringBuffer excludedUsersStrList = getExcludedUsersListForCurrentVertex(excludedUsers);
		
		 
		 
		
		/**
		 * For the first user step, send only the trust ratings as we don't need to
		 * worry about distrust ratings for count based semantics in this case.
		 */
	    if (getSuperstep() == 0) {
	    
		    if (vertex.getId() != null){
		    	 StringBuffer exMessage = new StringBuffer();				
				 //excludedSourceUsersMap.put(getId(), excludedUsers);
				 exMessage.append(EXCLUDEDSTART); // Format is ES<VertextId>:<List of excluded users separated by comma>;EE
				 exMessage.append(vertex.getId().get());
				 exMessage.append(":");
				 exMessage.append(excludedUsersStrList.toString());
				 exMessage.append(";");
				 exMessage.append(EXCLUDEDEND);
				 
				 for (Edge<LongWritable, DoubleWritable> edge : vertex.getEdges()) {
					 
					 // Populate excluded users
					if (edge.getValue().get() >= 1.0){ // For epinions since it's binary
						StringBuffer message = new StringBuffer();
						StringBuffer strDepthSourceRatings = new StringBuffer(); // Format is DS<Depth>:<SourceVertexId>,<RatingValue>#;DE
						strDepthSourceRatings.append(DEPTHSTART);
						strDepthSourceRatings.append(1);
						strDepthSourceRatings.append(":");
						strDepthSourceRatings.append(vertex.getId().get()+","+ edge.getValue().get()+"#");
						strDepthSourceRatings.append(";");
						strDepthSourceRatings.append(DEPTHEND);
						 message.append(exMessage.toString());
						 message.append(strDepthSourceRatings.toString());
						 
						
						//MapWritable depthSourceRatings = new MapWritable();
						//MapWritable sourceRating = new MapWritable();
						
						//sourceRating.put(getId(), edge.getValue());
						//depthSourceRatings.put(new IntWritable(1), sourceRating);
						//LocalPartialTrustMessageWritable message = new LocalPartialTrustMessageWritable(excludedSourceUsersMap, depthSourceRatings);
						sendMessage(edge.getTargetVertexId(), new Text(message.toString())); 
						
						
					}	
					
					
					 
				 }
		    }	 
		 
		 return;
	    	
	    }	
	    
	    
	    
	    
	    // If superstep > 0, should get messages. If there are no messages
	    // then this user is hanging and there are no trust ratings associated to
	    // it.
	    /**
	     * If super step > 0 and no incoming messages then there is nothing to process and vote for
	     * halt.
	     */
	    if (messages == null || messages.iterator() == null || !messages.iterator().hasNext()){
	    	vertex.voteToHalt();
	    	return;
	    }
	    
	    
	    Text currentState = vertex.getValue();
	    if (currentState == null){
	    	// Initialize the current state
	    	// MapWritable excludedSourceUsersMap = new MapWritable();
	    	 //MapWritable depthSourceRatings = new MapWritable();
	    	 //currentState = new LocalPartialTrustMessageWritable(excludedSourceUsersMap, depthSourceRatings);
	    	currentState = new Text();
	    }
	    populateCurrentState(currentState.toString());
	    
	    
	    /**
	     * Below code sets the vertex state with ratings
	     */
	    
	    // 1. Set the state of the vertex with ratings-Start
	    Map<Long, CountValue> sourceCountMap = new HashMap<Long, CountValue>(); // This is for count based semantics
	    int currentDepth = (int)getSuperstep();
	    for (Text message : messages) {
	    	
	    	// Update the depth, source and ratings at current depth.
	    	if (null != message){
		    	String strMessage = message.toString();
		    	//System.out.println("Received message at superstep:"+getSuperstep()+";VertexId:"+vertex.getId()+";Message value is:"+strMessage);
		    	Map<Integer, Map<Long,Double>> depthSourceRatings =getSourceRatingsAtDepth(strMessage);
		    	
		    	
		    	if (null != depthSourceRatings){
		    		//for (Integer depth : depthSourceRatings.keySet()){
		    			
		    			Map<Long,Double> sourceRatings = depthSourceRatings.get(currentDepth);
		    			if ( depthSourceRatingMap.get(currentDepth) == null){
		    				depthSourceRatingMap.put(currentDepth, new HashMap<Long,Double>());
		    			}
		    			Map<Long,Double> currentSourceRatings = depthSourceRatingMap.get(currentDepth);
		    			if (null != sourceRatings){
		    				for (Long sourceId: sourceRatings.keySet()){
		    					Double sourceRating = sourceRatings.get(sourceId);
		    					
		    					CountValue countValue = sourceCountMap.get(sourceId);
		    					if (countValue == null){
		    						countValue = new CountValue();
		    					}
								if (sourceRating>0){
									countValue.count+=1;
								} else if (sourceRating<0){
									countValue.count-=1;
								}
								if (sourceRating > countValue.rating){
									countValue.rating = sourceRating;
								}
								currentSourceRatings.put(sourceId,countValue.rating);
								/*if (countValue.count > 0){
									currentSourceRatings.put(sourceId,countValue.rating);
								} else if (countValue.count < 0){
									currentSourceRatings.put(sourceId, new Double(-200D));// Distrusts // TODO: Determine based on count to avoid overriding ratings
								} else {
									currentSourceRatings.put(sourceId, new Double(-100D));// Ambiguous
								}*/
								sourceCountMap.put(sourceId, countValue);
		    				}
		    				depthSourceRatingMap.put(currentDepth, currentSourceRatings);
		    			}
		    			
		    		}
		    	//}
		    	
		    	// 1. Set the state of the vertex with ratings -End
		    	
		    	// Set the current state with excluded users
		    	Map<Long,Set<Long>> excludedUserMap = getExcludedUsers(strMessage);
		    	if (null != excludedUserMap){
		    		for (Long lSource : excludedUserMap.keySet()){
		    			
		    			// Get excluded users at current state for the source
		    			// If null, initialize the map with empty array
		    			if (excludedUsersMap.get(lSource) == null){
		    				Set<Long> currentExcludedUsers = new TreeSet<Long>();
		    				excludedUsersMap.put(lSource,currentExcludedUsers );
		    			}
		    			
		    			// Get excluded users at current state for the source and add new entries uniquley
		    			Set<Long> currentExcludedUsers =  excludedUsersMap.get(lSource);
						currentExcludedUsers.addAll(excludedUserMap.get(lSource));
						excludedUsersMap.put(lSource,currentExcludedUsers );
		    		}
		    	}
	    	}
	    	
	    	
	    	
	    	
	    	
	    }
	    
	    // Aggregate ratings
	    Map<Long, Double> sourceAggrRatings = depthSourceRatingMap.get(currentDepth);
	    if (null != sourceAggrRatings){
	    	for (Long sourceId: sourceAggrRatings.keySet()){
	    		CountValue countValue = sourceCountMap.get(sourceId);
	    		if (countValue != null){
	    			if (countValue.count > 0){
	    				sourceAggrRatings.put(sourceId,countValue.rating);
					} else if (countValue.count < 0){
						sourceAggrRatings.put(sourceId, new Double(-200D));// Distrusts // TODO: Determine based on count to avoid overriding ratings
					} else {
						sourceAggrRatings.put(sourceId, new Double(-100D));// Ambiguous
					}
	    		}
	    		
	    	}
	    	depthSourceRatingMap.put(currentDepth, sourceAggrRatings);
	    }
	    
	    
	    
	   
	    if (getSuperstep() == maxSuperSteps){
	    	// If it's the third super step then set the state of vertex and 
	    	// Vote for halt
	    	String vertValue = loadVertexValue().toString();
			 LOG.info("Vertex value is:"+vertValue);
			// if (getSuperstep()>1)
				// System.out.println("End of Superstep:"+getSuperstep()+";VertexId:"+vertex.getId()+";Vertex value is:"+vertValue);
			 vertex.setValue(new Text(vertValue));
	    	vertex.voteToHalt();
	    	return;
	    }
	    
	    
	    // Send messages and compute trust
	    
		
		
		
		 for (Edge<LongWritable, DoubleWritable> edge : vertex.getEdges()) {
			 //System.out.println("Edge at superstep:"+getSuperstep()+";VertexId:"+vertex.getId()+";Edge target vertex is:"+edge.getTargetVertexId());
			// Map with depth, source ratings for the edge 
			
			Map<Integer, Map<Long,Double>> localDepthSourceRating = new HashMap<Integer, Map<Long,Double>>();
			Map<Long, Set<Long>> localExcludedMap = new HashMap<Long, Set<Long>>();
			// Map with excluded users for the edge
			
			Set<Long> usersAdded = new TreeSet<Long>();
			
			if (null != depthSourceRatingMap){
				//for (Integer depth : depthSourceRatingMap.keySet()){ //TODO: Avoid loop as for each super step we know the depth
					
					Map<Long,Double> sourceRatings = depthSourceRatingMap.get((int)getSuperstep());
					if (null != sourceRatings){
						//int iDepth = ((IntWritable)depth).get();
						int incrementedDepth = ((int)getSuperstep())+1;
						for (Long source : sourceRatings.keySet()){
							if (excludedUsersMap.get(source) != null){ // Check to make sure the edge is not in excluded list for source
								Set<Long> exList = excludedUsersMap.get(source) ;
								if (exList.contains(edge.getTargetVertexId().get())){
									continue;
								}
								Double currentSourceRating = sourceRatings.get(source);
								if (null != currentSourceRating && currentSourceRating<0){ // To deal with distrust and ambiguous trust
									continue;
								}
								
								// Get the minumum of current rating and edge rating
								Double msgRating = currentSourceRating < edge.getValue().get()?currentSourceRating:edge.getValue().get();
								Map<Long,Double> msgSourceRating;
								if (localDepthSourceRating.get(incrementedDepth) == null){
									msgSourceRating = new HashMap<Long,Double>();
									localDepthSourceRating.put(incrementedDepth, msgSourceRating);
								} 
								
							    msgSourceRating= localDepthSourceRating.get(incrementedDepth);
								msgSourceRating.put(source, msgRating);
								localDepthSourceRating.put(incrementedDepth, msgSourceRating);
								usersAdded.add(source);
							}
						}
					}
				 // }	
					
				}
			
			
				// Add excluded users
				for (Long userId : usersAdded){
					//LongWritable lUserId = new LongWritable(userId);
					if (excludedUsersMap.get(userId) != null){
						Set<Long> l_exUsers = excludedUsersMap.get(userId);
						l_exUsers.addAll(excludedUsers);
						localExcludedMap.put(userId, l_exUsers);
					} else {
						localExcludedMap.put(userId, excludedUsers);
					}
				}	
				//LocalPartialTrustMessageWritable message = new LocalPartialTrustMessageWritable(msgExcludedMap, msgDepthSourceRatings);	
				Text messageToSend = contructMessageToSend (localExcludedMap, localDepthSourceRating);
				//System.out.println("Sent message at superstep:"+getSuperstep()+";VertexId:"+vertex.getId()+";Message value is:"+messageToSend.toString());
				if (messageToSend != null && messageToSend.toString().trim().length()>0)
					sendMessage(edge.getTargetVertexId(), messageToSend); 
			
				
			}
		 String vertValue = loadVertexValue().toString();
		 LOG.info("Vertex value is:"+vertValue);
		// if (getSuperstep()>1)
			// System.out.println("End of Superstep:"+getSuperstep()+";VertexId:"+vertex.getId()+";Vertex value is:"+vertValue);
		 vertex.setValue(new Text(vertValue));
	  } catch (Throwable t){
		  t.printStackTrace();
		 
	  }
	 
    
  }
  
  
  private StringBuffer getExcludedUsersListForCurrentVertex(Set<Long> excludedUsers)
	  {
	 
		 
		 StringBuffer excludedUsersStrList = new StringBuffer();
		 boolean isFirst = true;
		 for (Long longWritable : excludedUsers){
			 if (!isFirst){
				 excludedUsersStrList.append(",");
				 
			 } else {
				 isFirst= false;
			 }
			 excludedUsersStrList.append(longWritable);
			 
			 
		 }
		 if (excludedUsers.size() == 1){
			 excludedUsersStrList.append(","); 
		 }
		 return excludedUsersStrList;
  }
  
  private StringBuffer loadVertexValue(){
	  
	  StringBuffer vertValue = new StringBuffer();
	  if (depthSourceRatingMap != null && depthSourceRatingMap.size() >0){
		  vertValue.append(DEPTHSTART);
		   for (Integer depth : depthSourceRatingMap.keySet()){
			   Map<Long,Double> sourceRatingMap = depthSourceRatingMap.get(depth);
			   for (Long sourceId : sourceRatingMap.keySet()){
				   vertValue.append(depth);
				   vertValue.append(":");
				   vertValue.append(sourceId+","+ sourceRatingMap.get(sourceId)+"#");
				   vertValue.append(";");
					
			   }
		   }
		   vertValue.append(DEPTHEND);
	  }
	  if (excludedUsersMap != null && excludedUsersMap.size()>0){
		  	 vertValue.append(EXCLUDEDSTART); // Format is ES<VertextId>:<List of excluded users separated by comma>;EE
		  	 for (Long sourceId: excludedUsersMap.keySet()){
			  	 vertValue.append(sourceId);
				 vertValue.append(":");
				 boolean isFirst=true;
				 for (Long exUser: excludedUsersMap.get(sourceId)){
					 vertValue.append(exUser);
					 vertValue.append(",");
				 }
				 
				 vertValue.append(";");
		  	 }
			 vertValue.append(EXCLUDEDEND);
	  }
	  
	  return vertValue;
  }
  
  private Text contructMessageToSend(Map<Long,Set<Long>> localExcludedMap, Map<Integer,Map<Long, Double>>  localDepthSourceRating){
	  StringBuffer buffer = new StringBuffer();
	  if (null != localExcludedMap && localExcludedMap.size()>0){
		//excludedSourceUsersMap.put(getId(), excludedUsers);
		  buffer.append(EXCLUDEDSTART);
		  
		  for (Long source : localExcludedMap.keySet()){
			  buffer.append(source);
			  buffer.append(":");
			  Set<Long> exUsers = localExcludedMap.get(source);
			  if (null != exUsers){
				  boolean isFirst = true;
				  for (Long exUser: exUsers){
					  if (!isFirst){
							 buffer.append(",");
							 
						 } else {
							 isFirst= false;
						 }
						 buffer.append(exUser);
				  }
				  if (exUsers.size() ==1){
					  buffer.append(",");
				  }
				  buffer.append(";");
			  }
		  }
		  buffer.append(EXCLUDEDEND);	 
	  }
	  
	  if (null != localDepthSourceRating && localDepthSourceRating.size()>0){
		  buffer.append(DEPTHSTART);
		  
		  for (Integer depth : localDepthSourceRating.keySet()){
			  buffer.append(depth);
			  buffer.append(":");
			  Map<Long, Double> sourceRatings =  localDepthSourceRating.get(depth);
			  if (null != sourceRatings && sourceRatings.size()>0){
				  boolean isFirst = true;
				  for (Long source: sourceRatings.keySet()){
					  if (!isFirst){
						  buffer.append("#");
					  } else {
						  isFirst = false;
					  }
					  buffer.append(source+",");
					  buffer.append(sourceRatings.get(source));
				  }
				  if (sourceRatings.size() ==1){
					  buffer.append("#");
				  }
				  buffer.append(";");
			  }
		  }
		  buffer.append(DEPTHEND);
	  }
	  return new Text(buffer.toString());
	  
	  
  }
  
  
  private void populateCurrentState (String message){
	  if (null == message){
		  return;
	  }
	  // Populates the excluded users for each user
	 populateExcludedUsers(message);
	 // Populates the ratings of other vertices to this vertex at each depth
	 populateDepthSourceRatings(message);
  }
  
  
  private void populateExcludedUsers(String message){
	  
	  if (message == null || message.indexOf(EXCLUDEDSTART)==-1){
		  return;
	  }
	  String excluded = message.substring(message.indexOf(EXCLUDEDSTART)+EXCLUDEDSTART.length(), message.indexOf(EXCLUDEDEND));
	  if (null != excluded){
		  String[] parentSplits = excluded.split(";");
		  if (null != parentSplits && parentSplits.length >0){
			  for (String parentSplit : parentSplits){
				  String[] childSplits = parentSplit.split(":");
				  if (null != childSplits && childSplits.length>0){
					  Long userId = Long.valueOf(childSplits[0]);
					  String strExUserSplit = childSplits[1];
					  Set<Long> exUsers;
					  if (excludedUsersMap.get(userId) != null){
						 exUsers= excludedUsersMap.get(userId);
					  } else {
						  exUsers= new TreeSet<Long>();
					  }
					  String[] exUsersSplits = strExUserSplit.split(",");
					  if (null != exUsersSplits){
						  for (String exUser : exUsersSplits){
							  exUsers.add(Long.valueOf(exUser));
						  }
					  }
					  excludedUsersMap.put(userId, exUsers);
				  }
			  }
		  }
	  }
  }
  
  
  /**
   * Parses the message and populates the depth source ratings to depthSourceRatingMap
   * @param message
   */
  private void populateDepthSourceRatings(String message){
	  if (message == null || message.indexOf(DEPTHSTART)==-1){
		  return;
	  }
	  String excluded = message.substring(message.indexOf(DEPTHSTART)+DEPTHSTART.length(), message.indexOf(DEPTHEND));
	  if (null != excluded){
		  String[] parentSplits = excluded.split(";");
		  if (null != parentSplits && parentSplits.length >0){
			  for (String parentSplit : parentSplits){
				  String[] childSplits = parentSplit.split(":");
				  if (null != childSplits && childSplits.length>0){
					  Integer depth = Integer.valueOf(childSplits[0]);
					  String strExUserSplit = childSplits[1];
					  
					  Map<Long,Double> sourceRatings;
					  if (depthSourceRatingMap.get(depth) != null){
						  sourceRatings= depthSourceRatingMap.get(depth);
					  } else {
						  sourceRatings= new HashMap<Long,Double>();
					  }
					  String[] sourceRatingSplits = strExUserSplit.split("#");
					  if (null != sourceRatingSplits){
						  for (String srcRatingSplit : sourceRatingSplits){
							  String[] exUsersSplits = srcRatingSplit.split(",");
							  if (null != exUsersSplits){
								  if (exUsersSplits.length != 2){
									  LOG.debug("Invalid message");
								  } else {
									  sourceRatings.put(Long.valueOf(exUsersSplits[0]), Double.valueOf(exUsersSplits[1]));
								  }
								 
							  } 
						  }
					  }
					  
					  depthSourceRatingMap.put(depth, sourceRatings);
				  }
			  }
		  }
	  }
  }
  
  
  private Map<Integer,Map<Long,Double>> getSourceRatingsAtDepth (String message){
	  
	  Map<Integer,Map<Long,Double>> retVal = new HashMap<Integer,Map<Long,Double>>();
	  if (message == null || message.indexOf(DEPTHSTART)==-1){
		  return retVal;
	  }
		 
	  String excluded = message.substring(message.indexOf(DEPTHSTART)+DEPTHSTART.length(), message.indexOf(DEPTHEND));// Format is DS<Depth>:<SourceVertexId>,<RatingValue>#;DE
	  if (null != excluded){
		  String[] parentSplits = excluded.split(";");
		  if (null != parentSplits && parentSplits.length >0){
			  for (String parentSplit : parentSplits){
				  String[] childSplits = parentSplit.split(":");
				  if (null != childSplits && childSplits.length>0){
					  Integer depth = Integer.valueOf(childSplits[0]);
					  String strExUserSplit = childSplits[1];
					  
					  Map<Long,Double> sourceRatings;
					  if (retVal.get(depth) != null){
						  sourceRatings= retVal.get(depth);
					  } else {
						  sourceRatings= new HashMap<Long,Double>();
					  }
					  String[] sourceRatingSplits = strExUserSplit.split("#");
					  if (null != sourceRatingSplits){
						  for (String srcRatingSplit : sourceRatingSplits){
							  String[] exUsersSplits = srcRatingSplit.split(",");
							  if (null != exUsersSplits){
								  if (exUsersSplits.length != 2){
									  LOG.debug("Invalid message");
								  } else {
									  sourceRatings.put(Long.valueOf(exUsersSplits[0]), Double.valueOf(exUsersSplits[1]));
								  }
								 
							  } 
						  }
					  }
					  retVal.put(depth, sourceRatings);
				  }
			  }
		  }
	  }
	  return retVal;
  }
  
  
  private  Map<Long, Set<Long>> getExcludedUsers(String message){
	Map<Long, Set<Long>> retVal = new TreeMap<Long, Set<Long>>();
	if (message == null || message.indexOf(EXCLUDEDSTART)==-1){
		  return retVal;
	  }
	  String excluded = message.substring(message.indexOf(EXCLUDEDSTART)+EXCLUDEDSTART.length(), message.indexOf(EXCLUDEDEND));
	  if (null != excluded){
		  String[] parentSplits = excluded.split(";");
		  if (null != parentSplits && parentSplits.length >0){
			  for (String parentSplit : parentSplits){
				  String[] childSplits = parentSplit.split(":");
				  if (null != childSplits && childSplits.length>0){
					  Long userId = Long.valueOf(childSplits[0]);
					  String strExUserSplit = childSplits[1];
					  Set<Long> exUsers;
					  if (retVal.get(userId) != null){
						 exUsers= retVal.get(userId);
					  } else {
						  exUsers= new TreeSet<Long>();
					  }
					  String[] exUsersSplits = strExUserSplit.split(",");
					  if (null != exUsersSplits){
						  for (String exUser : exUsersSplits){
							  exUsers.add(Long.valueOf(exUser));
						  }
					  }
					  retVal.put(userId, exUsers);
				  }
			  }
		  }
	  }
	  return retVal;
  }



	public static void main (String[] args){
		
		String test = "ES100:100,101,102,103,104;ENDS1:100,1.0#;DE";
		
			//System.out.println(getExcludedUsers(test));
		
	}
  
  
  
  
  
  
  
}

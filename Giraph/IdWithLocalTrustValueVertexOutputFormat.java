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

package org.apache.giraph.io.formats;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


import org.apache.giraph.graph.Vertex;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.log4j.Logger;

/**
 * Write out Vertices' IDs and values, but not their edges nor edges' values.
 * This is a useful output format when the final value of the vertex is
 * all that's needed. The boolean configuration parameter reverse.id.and.value
 * allows reversing the output of id and value.
 *
 * @param <I> Vertex index value
 * @param <V> Vertex value
 * @param <E> Edge value
 */
@SuppressWarnings("rawtypes")
public class IdWithLocalTrustValueVertexOutputFormat
    <I extends WritableComparable,
    V extends Writable, E extends Writable>
    extends TextVertexOutputFormat<I, V, E> {
	
	
	private static final Logger LOG =
		      Logger.getLogger(IdWithLocalTrustValueVertexOutputFormat.class);

  /** Specify the output delimiter */
  public static final String LINE_TOKENIZE_VALUE = "output.delimiter";
  /** Default output delimiter */
  public static final String LINE_TOKENIZE_VALUE_DEFAULT = "\t";
  /** Reverse id and value order? */
  public static final String REVERSE_ID_AND_VALUE = "reverse.id.and.value";
  /** Default is to not reverse id and value order. */
  public static final boolean REVERSE_ID_AND_VALUE_DEFAULT = false;
  
  private static final String DEPTHSTART = "DS";
  private static final String DEPTHEND = "DE";

  @Override
  public TextVertexWriter createVertexWriter(TaskAttemptContext context) {
    return new IdWithValueVertexWriter();
  }

  /**
   * Vertex writer used with {@link IdWithValueTextOutputFormat}.
   */
  protected class IdWithValueVertexWriter extends TextVertexWriterToEachLine {
    /** Saved delimiter */
    private String delimiter;
    /** Cached reserve option */
    private boolean reverseOutput;

    @Override
    public void initialize(TaskAttemptContext context) throws IOException,
        InterruptedException {
      super.initialize(context);
      Configuration conf = context.getConfiguration();
      delimiter = conf
          .get(LINE_TOKENIZE_VALUE, LINE_TOKENIZE_VALUE_DEFAULT);
      reverseOutput = conf
          .getBoolean(REVERSE_ID_AND_VALUE, REVERSE_ID_AND_VALUE_DEFAULT);
    }

    @Override
    protected Text convertVertexToLine(Vertex<I, V, E> vertex)
      throws IOException {
      String first;
      String second;
      Text trustWritable;
      if (reverseOutput) {
        first = vertex.getValue().toString();
        second = vertex.getId().toString();
      } else {
    	second="";
        first = vertex.getId().toString();
        trustWritable   = (Text)vertex.getValue();
        if (trustWritable != null){
	        Map<Integer, Map<Long, Double>> depthSourceRatings = getSourceRatingsAtDepth(trustWritable.toString());
	        if (depthSourceRatings != null){
	        	//MapWritable depthSourceRatings = (MapWritable)trustWritable.getSourceRatingsAtDepth();
	        	for (Integer depth: depthSourceRatings.keySet()){
	        		//IntWritable iDepth = (IntWritable)depth;
	        		second = depth+"#";
	        		if (depthSourceRatings.get(depth) != null){
	        			Map<Long, Double> sourceRatings = depthSourceRatings.get(depth);
	        			for (Long source: sourceRatings.keySet()){
	        				//LongWritable lSource = (LongWritable)source;
	        				Double dRating = sourceRatings.get(source);
	        				if (!second.endsWith("#")){
	        					second+=",";
	        				}
	        				second+= source+":"+dRating;
	        			}
	        			second+=";";
	        		}
	        		
	        		
	        		
	        	}
	        }
        }   
      }
      Text line = new Text(first + delimiter + second);
      return line;
    }

  }
  
  
private Map<Integer,Map<Long,Double>> getSourceRatingsAtDepth (String message){
	  
	  Map<Integer,Map<Long,Double>> retVal = new HashMap<Integer,Map<Long,Double>>();
	  if (message == null || message.indexOf(DEPTHSTART)==-1){
		  return retVal;
	  }
	  LOG.debug("Output message is:"+message);
	  System.out.println("Output message is:"+message);
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

}

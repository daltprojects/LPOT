package org.apache.giraph.io.formats;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Writable;
/**
 * 
 * @author dalthuru
 *
 */
public class LocalPartialTrustMessageWritable implements Writable {
	
	
	// Key is Long Writable
	// Value is ArrayWritable holding Long values
	private MapWritable excludedUserMap;
	
	// Key is LongWritable having sourceId
	// Value is MapWritable having key as depth (FloatWritable) and value rating.
	private MapWritable sourceRatingsAtDepth;
	
	public LocalPartialTrustMessageWritable(MapWritable excludedUserMap, MapWritable sourceRatingsAtDepth) {
	    set(excludedUserMap, sourceRatingsAtDepth);
	  }
	
	public LocalPartialTrustMessageWritable(){
		MapWritable depthSourceRatings = new MapWritable();
		MapWritable sourceRating = new MapWritable();
		sourceRating.put(new LongWritable(1), new DoubleWritable(1.0));
		depthSourceRatings.put(new IntWritable(1), sourceRating);
		
		MapWritable excludedSourceUsersMap = new MapWritable();
		 List<Writable> excludedUsersList = new ArrayList<Writable> ();	
		 LongArrayWritable excludedUsers = new LongArrayWritable();  
		 excludedUsersList.add(new LongWritable(1));
		 excludedUsers.set(excludedUsersList.toArray(new LongWritable[excludedUsersList.size()]));
		excludedSourceUsersMap.put(new LongWritable(1), excludedUsers);
		set (excludedSourceUsersMap, new MapWritable());
	}
	  
	  public void set(MapWritable excludedUserMap, MapWritable sourceRatingsAtDepth) {
	    this.excludedUserMap = excludedUserMap;
	    this.sourceRatingsAtDepth = sourceRatingsAtDepth;
	  }
	  
	  public MapWritable getExcludedUserMap() {
		    return excludedUserMap;
		  }
	
	  public MapWritable getSourceRatingsAtDepth() {
	    return sourceRatingsAtDepth;
	  }

	@Override
	public void readFields(DataInput in) throws IOException {
		
		excludedUserMap.readFields(in);
		sourceRatingsAtDepth.readFields(in);
		/*
		// TODO Auto-generated method stub
		if (null != excludedUserMap)
			excludedUserMap.readFields(in);
		else {
			MapWritable defaultMap = new MapWritable();
			defaultMap.put(new LongWritable(), new LongArrayWritable());
			defaultMap.readFields(in);
		}	
		if (null != sourceRatingsAtDepth)
			sourceRatingsAtDepth.readFields(in);
		else {
			MapWritable defaultMap = new MapWritable();
			MapWritable innerMap = new MapWritable();
			innerMap.put(new LongWritable(), new DoubleWritable());
			defaultMap.put(new IntWritable(), innerMap);
			defaultMap.readFields(in);
		}*/	
	}

	@Override
	public void write(DataOutput out) throws IOException {
		// TODO Auto-generated method stub
		if (null != excludedUserMap){
		excludedUserMap.write(out);
		} else {
			new MapWritable().write(out);
		}
	if (null != sourceRatingsAtDepth){
		sourceRatingsAtDepth.write(out);
	} else {
		new MapWritable().write(out);
	}
		
			
	}

	
	
	@Override
	  public int hashCode() {
	    return excludedUserMap.hashCode() * 163 + sourceRatingsAtDepth.hashCode();
	  }
	  
	  @Override
	  public boolean equals(Object o) {
	    if (o instanceof LocalPartialTrustMessageWritable) {
	    	LocalPartialTrustMessageWritable tp = (LocalPartialTrustMessageWritable) o;
	      return excludedUserMap.equals(tp.excludedUserMap) && sourceRatingsAtDepth.equals(tp.sourceRatingsAtDepth);
	    }
	    return false;
	  }
	
	  
	  
	

}

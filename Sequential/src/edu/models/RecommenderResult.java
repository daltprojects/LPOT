package edu.models;

public class RecommenderResult {
	
	public enum AlgorithmType {Tidal, PartialOrder, ModPartialOrder}
	public enum TrustResult {AMBIGOUS, NO_INFO, TRUSTS, DISTRUSTS, TIMED_OUT, DEFAULT}
	public Float itemRating;
	public TrustResult trustResult = TrustResult.DEFAULT;
	public AlgorithmType algorithmType;
	

}

package de.uni_potsdam.hpi.bpt.search.evaluation;

/**
 * Interface for a data point of a {@link SearchResult}, i.e., a pair of query 
 * and candidate and their distance.
 * 
 * Licensed under the MIT License for Open Source Software, 
 * <http://opensource.org/licenses/MIT>.
 * Copyright (c) 2013, Matthias Kunze. 
 *  * 
 * @author <mtkunze@gmail.com>
 */
public interface Datapoint {
	
	/**
	 * Returns whether the given document is relevant for the given query
	 * 
	 * @return
	 */
	public boolean isRelevant();
	
	/**
	 * Get the distance between query and candidate.
	 * @return
	 */
	public double  getDistance();
	
	/**
	 * Implementation for the equality operator, i.e., if two data points with 
	 * the same query, candidate, and distance are created, they should be 
	 * equal.
	 * 
	 * Required to compare different search results.  
	 * 
	 * @param other
	 * @return
	 */
	public boolean equals(Datapoint other);
}

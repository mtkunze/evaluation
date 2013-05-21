package de.uni_potsdam.hpi.bpt.search.evaluation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import de.uni_potsdam.hpi.bpt.util.Combinatorics;

/**
 * Computes a set of quality evaluation measures for Process Model Search Results.
 * 
 * Sources:
 * 
 * [1] M. Guentert, M. Kunze, and M. Weske. Evaluation Measures for Similarity 
 *     Search Results in Process Model Repositories. In ER 2012, volume 7532 of
 *     Lecture Notes in Computer Science, pages 214-227. Springer, 2012.
 *     
 * [2] M. Kunze and M. Weske. Methods for Evaluating Process Model Search. 
 *     Submitted to PMC-MR'13 (4th International Workshop on Process Model 
 *     Collections: Management and Reuse)
 *     
 * Further measures can be found in:
 *     
 * C. Buckley and E. M. Voorhees. Evaluating Evaluation Measure Stability. 
 * In ACM SIGIR '00, pages 33-40, New York, NY, USA, 2000. ACM.
 *     
 * J. Euzenat and P. Shvaiko. Ontology Matching. Springer, 2007.
 *     
 * C. D. Manning, P. Raghavan, and H. Schuetze. Introduction to Information 
 * Retrieval. Cambridge University Press, 1 edition, July 200
 *   
 * Licensed under the MIT License for Open Source Software, 
 * <http://opensource.org/licenses/MIT>.
 * Copyright (c) 2013, Matthias Kunze. 
 * 
 *  
 * @author <mtkunze@gmail.com>
 *
 * @param <T>
 */

public class SearchResult<T extends Datapoint> extends LinkedList<T> implements List<T> {

	private static final long serialVersionUID = 5322382330632546886L;
	
	protected int numberOfRelevant = -1;
	protected Comparator<T> comparator = null;

	/**
	 * Constructor for search result that is already ordered.
	 * Effectiveness measures that require ordering, avgPrecision, rPreciscion, will use the insertion order  
	 * 
	 * @param numberOfRelevant
	 */
	public SearchResult(int numberOfRelevant) {
		this.numberOfRelevant = numberOfRelevant;
	}
	
	/**
	 * Constructor for search results that is not ordered yet. 
	 * Effectiveness measures that require ordering, avgPRecision, rPrescision, will sort the list before calculation.
	 * 
	 * @param numberOfRelevant
	 * @param comparator A comparator to order the search result. 
	 */
	public SearchResult(int numberOfRelevant, Comparator<T> comparator) {
		this(numberOfRelevant);
		
		if (null == comparator) {
			throw new IllegalArgumentException("Comparator must not be null");
		}
		this.comparator = comparator; 
	}
	
	/**
	 * Internal Constructor to create sublists, accepts also comparators that are null (must be in reversedOrder already)
	 * 
	 * @param numberOfRelevant
	 * @param comparator
	 * @param elements
	 */
	protected SearchResult(int numberOfRelevant, Comparator<T> comparator, List<T> elements) {
		this(numberOfRelevant);
		
		this.comparator = comparator;
		this.addAll(elements);
	}
	
	/**
	 * Sorts the collection, iff comparator is given.
	 */
	protected void sort() {
		if (null != this.comparator) {
			Collections.sort(this, this.comparator);
		}
	}
	
	/**
	 * Computes, the number of data points found that are relevant.
	 * @return
	 */
	public int foundRelevant() {
		int number = 0;
		for (T c : this) {
			if (c.isRelevant()) {
				number++;
			}
		}
		return number;
	}
		
	/**
	 * Computes the precision [2], i.e., the ratio found data points that are 
	 * relevant. 
	 *  
	 * @return
	 */
	public double precision() {
		if (0 == this.numberOfRelevant || 0 == this.size()) {
			return 1;
		}
				
		return this.foundRelevant()/(double)this.size();
	}
	
	/**
	 * Computes the precision [2] for the first k data points.
	 * 
	 * @param k
	 * @return
	 */
	public double precision(int k) {
		assert k <= this.size() : "Cannot calculate precision for subset larger than search result";
		
		if (k == this.size()) {
			return this.precision();
		}
		
		SearchResult<T> list = new SearchResult<T>(this.numberOfRelevant);
		this.sort();
		
		for (T c : this) {
			list.add(c);
			if (--k == 0) {
				break;
			}
		}
		
		return list.precision();
	}
	
	/**
	 * Computes the precision after r documents have been found [2], where r is
	 * the number of relevant documents.
	 * 
	 * @return
	 */
	public double rPrecision() {
		return this.precision(this.numberOfRelevant);
	}
	
	/**
	 * Computes the average precision [2], i.e., the average of each precision,
	 * calculated every time a relevant data point was found.
	 * 
	 * @return
	 */
	public double avgPrecision() {
		if (0 == this.size()) {
			return 1;
		}
		
		this.sort();
		
		int relevant = 0;
		int count = 0;
		double aggPrec = 0;
		
		for (T c : this) {
			relevant += c.isRelevant() ? 1 : 0;
			count ++;
			
			double prec = relevant / (double) count;
			aggPrec += c.isRelevant() ? prec : 0;
		}
		
		return aggPrec / (double) this.numberOfRelevant;
	}
	
	/**
	 * Computes the precision for a given recall value [2].
	 * @param recall recall level to base the precision upon, must be within [0,1]
	 * @return
	 */
	public double precisionAtRecall(double recall) {
		assert 0 <= recall && recall <= 1 : "Recall must be within [0,1], is " + recall;
		
		if (0 == recall || 0 == this.size()) {
			return 1;
		}
		
		this.sort();
		
		int relevant = 0;
		int count = 0;
		
		for (T c : this) {
			relevant += c.isRelevant() ? 1 : 0;
			count ++;
			
			if (relevant/(double)this.numberOfRelevant >= recall) {
				return relevant/(double)count; 
			}
		}
		
		// if recall level cannot be reached, it would have a precision of 0
		return 0;
	}
	
	/**
	 * Computes the recall [2], i.e., the ratio of relevant data points that 
	 * were found.
	 *  
	 * @return
	 */
	public double recall() {
		if (0 == this.size()) {
			return 0;
		}
		
		return this.foundRelevant()/(double)this.numberOfRelevant;
	}
	
	/**
	 * Measures the accuracy (or effectiveness) of retrieval, which is the 
	 * harmonic mean of precision and recall.
	 * @return
	 */
	public double fMeasure() {
		return this.fMeasure(1);
	}
	
	/**
	 * f-score measures the effectiveness of retrieval with respect to a user 
	 * who attaches 1/beta times as much importance to recall as precision.
	 *  
	 * @param beta
	 * @return
	 */
	public double fMeasure(double beta) {
		double precision = this.precision();
		double recall = this.recall();
		
		return (1+beta)*(precision*recall)/(beta*precision + recall);
	}
	
	/**
	 * Measures the confidence of the first result [1], i.e., how good the best
	 * result is compared to the rest. 
	 * 
	 * @return 
	 */
	public double confidenceFirst() {
		testOrdering();
		
		if (this.size() < 2) {
			return 0;
		}
		
		return 1f - this.dMin()/this.dMed();
	}
	
	/**
	 * Measures the confidence of the superior results [1], i.e., how good the
	 * better half of the results is compared to the worse.
	 * 
	 * @return
	 */
	public double confidenceMost() {
		testOrdering();
		
		if (this.size() < 2) {
			return 0;
		}
		
		int k = (int) this.size()/2;
		SearchResult<T> sup = this.subList(0,k+1); // sublist is somehow strange, toIndex refers to the first not included element
		SearchResult<T> inf = this.subList(k+1, this.size());
		
		return 1f - sup.dMed()/inf.dMed();
	}
	
	/**
	 * Measures how well the better half of the search result can be ranked
	 * compared to the rest of the results [1].
	 * 
	 * @return
	 */
	public double discriminationMost() {
		testOrdering();
		
		double interval = this.dMax() - this.dMin();
		if (interval <= 0) {
			return 0;
		}
		
		return (this.dMed() - this.dMin()) / 
		       interval;
	}
	
	/**
	 * Measures how evenly the ranking of results is distributed [1].
	 * 
	 * @return
	 */
	public double discriminationAll() {
		testOrdering();
		
		double interval = this.dMax() - this.dMin();
		if (interval <= 0) {
			return 0;
		}
		
		this.sort();
		double score = 0;
		double avg = interval/(this.size()-1);
		
		for (int i=1; i<this.size();i++) {
			
			double s = Math.abs(Math.abs(this.get(i-1).getDistance() - this.get(i).getDistance()) - avg);
			score += s;
		}
		
		return 1 - score / (2*interval);
	}
	
	/**
	 * tests the ordering, required for characteristic distances
	 */
	private void testOrdering() {
		if (this.dMax() < this.dMin()) {
			throw new IllegalStateException("Minimum distance is larger than maximum distance, check comparator!");
		}
	}
	
	/**
	 * Characteristic distance that determines the minimal distance of a match 
	 * to the query [1].
	 * 
	 * @return
	 */
	protected double dMin() {
		this.sort();
		return this.getFirst().getDistance();
	}
	
	
	/**
	 * Characteristic distance that determines the median distance of a match 
	 * to the query [1].
	 * 
	 * @return
	 */
	protected double dMed() {
		this.sort();
		return this.get((int) this.size()/2).getDistance();
	}
	
	/**
	 * Characteristic distance that determines the maximum distance of a match 
	 * to the query [1].
	 * 
	 * @return
	 */
	protected double dMax() {
		this.sort();
		return this.getLast().getDistance();
	}
	
	/**
	 *  Returns a view of the portion of this SearchResult between the 
	 *  specified fromIndex, inclusive, and toIndex, exclusive.
	 *  See {@link List#subList(int, int)}.
	 * 
	 * @param fromIndex - low endpoint (inclusive) of the subList
	 * @param toIndex - high endpoint (EXCLUSIVE) of the subList
	 */
	public SearchResult<T> subList(int fromIndex, int toIndex) {
		this.sort();
		return new SearchResult<T>(
			this.numberOfRelevant, 
			this.comparator, 
			super.subList(fromIndex, toIndex));
	}
	
	/**
	 * Returns the ranking position of a match in the search result [1].
	 * 
	 * @param t
	 * @return
	 */
	public int getRank(T t) { 
		this.sort(); 		
		for (int i=0; i<this.size();i++) {
			if (t.equals(this.get(i))) { 
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Ranking agreement measures the mutual agreement of a set of search 
	 * results on the ranking of identical matches [1].
	 *    
	 * @param others SearchResults to compare with, must contain the identical
	 *               set of matches
	 * @return
	 */
	public double rankingAgreement(Collection<SearchResult<T>> others) {
		List<SearchResult<T>> rankings = new ArrayList<SearchResult<T>>(others);
		rankings.add(this);
		
		int score = 0;
		
		for (int u=0; u < rankings.size(); u++) {
			for (int v=u+1; v < rankings.size(); v++) {
				for (int i=0; i< this.size(); i++) {
					
					T d = this.get(i);
					int ru = rankings.get(u).getRank(d);
					int rv = rankings.get(v).getRank(d);
					
					if (ru < 0 || rv < 0) {
						throw new IllegalStateException("Missing data point");
					}

					score +=  Math.abs(ru -rv);
				}
			}
		}
		
		double denominator = Combinatorics.binomialCoefficient(rankings.size(), 2) *
				1.0/4.0 * (2* (this.size()*this.size()) + (Math.pow(-1, this.size()) - 1));

		return 1 - score / denominator; 
	}
	
	/**
	 * Ranking agreement measures the mutual agreement with another search 
	 * result on the ranking of identical matches [1].
	 *    
	 * @param other SearchResult to compare with, must contain the identical
	 *               set of matches
	 * @return
	 */
	public double rankingAgreement(SearchResult<T> other) {
		HashSet<SearchResult<T>> others = new HashSet<SearchResult<T>>();
		others.add(other);
		return this.rankingAgreement(others);
	}
	
	/**
	 * Overlap computes the ratio of common matches within the top k 
	 * matches [2].  
	 * 
	 * @param other the search result to compare with
	 * @param k number of top k matches
	 * @return
	 */
	public double overlap(SearchResult<T> other, int k) {
		int overlap = 0;
		int max = Math.min(Math.min(k,  this.size()), other.size());
		
		this.sort();
		other.sort();
				
		for (int i=0; i<max; i++) {
			for (int j=0; j<max; j++) {
				if (this.get(i).equals(other.get(j))) {
					overlap++;
				}
			}
		}
			
		return overlap/((double) k);
	}
}

package de.uni_potsdam.hpi.bpt.search.evaluation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Calculates certain aggregates over a set of collected values for experimental
 * evaluation of any kind.  
 * 
 * Licensed under the MIT License for Open Source Software, 
 * <http://opensource.org/licenses/MIT>.
 * Copyright (c) 2013, Matthias Kunze. 
 * 
 * @author Matthias Kunze <mtkunze@gmail.com>
 *
 * @param <T> the type of aggregated values, must be a java.lang.Number
 */
public class Aggregate<T extends Number> extends ArrayList<T> implements List<T>, Serializable {

	private static final long serialVersionUID = -9128724712173041680L;
	protected final static Comparator<Number> Comp = new Comparator<Number>() {

		@Override
		public int compare(Number o1, Number o2) {
			return Double.compare(o1.doubleValue(), o2.doubleValue());
		}
	};

	/**
	 * Constructs an empty aggregate list.
	 */
	public Aggregate() {
		super();
	}
	
	/**
	 * Constructs an aggregate list containing the elements of the specified collection, 
	 * in the order they are returned by the collection's iterator.
	 * 
	 * @param c - the collection whose elements are to be placed into this list
	 */
	public Aggregate(Collection<T> c) {
		this.addAll(c);
	}
	
	/**
	 * Get the minimum of all collected values
	 * @return
	 */
	public T min() {
		Collections.sort(this, Comp);
		return this.get(0);
	}
	
	/**
	 * Get the maximum of all collected values
	 * @return
	 */
	public T max() {
		Collections.sort(this, Comp);
		return this.get(this.size()-1);
	}
	
	/**
	 * Get the average value of all collected values
	 * @return
	 */
	public double avg() {
		return this.sum() / (double)this.size();
	}
	
	/**
	 * Get the sum over all elements.
	 * 
	 * @return
	 */
	public  double sum() {
		
		double sum = 0;
		for (T value : this) {
			sum += value.doubleValue();
		}
		return sum;
	}
	
	/**
	 * Get the median value of all collected values, i.e., the value of which 
	 * there exist equally many elements that are lesser as such that are 
	 * greater.
	 * If no such value exists, the average of the adjacent values will be
	 * calculated.
	 *  
	 * @return
	 */
	public double median() {
		return this.quantile(0.5);
	}
	
	/**
	 * get the p-quantile of all collected values, i.e., the value of which 
	 * there exist (p*100)% elements that are lesser and ((1-p*100))% elements
	 * that are greater.
	 * If no such value exists, the p-quantile average of the adjacent values 
	 * will be calculated.
	 *  
	 * @param p the quantile fraction, must be 0 <= p <= 1
	 * @return
	 */
	public double quantile(double p) {
		
		if (this.size() == 0) {
			throw new IllegalStateException("Quantiles cannot be calculated for empty aggregations.");
		}
		
		if (this.size() == 1) {
			return this.get(0).doubleValue();
		}
		
		if (0 >= p) {
			return this.min().doubleValue();
		}
		
		if (1 <= p) {
			return this.max().doubleValue();
		}
				
		Collections.sort(this, Comp);
		int size = this.size();
		double pos = size * p; 
		Number[] sd = this.toArray(new Number[size]);
		
		if ((int)p == p) {
			return sd[(int)pos].doubleValue();
		}
		else {
			return p*sd[(int)pos].doubleValue() + (1-p)*sd[Math.min((int)pos + 1, size-1)].doubleValue();
		}
	}
	
	/**
	 * Puts data in a map, where the key is each unique value in this collection
	 * and the value is the number of occurrences of this value.
	 * 
	 * @return
	 */
	public Map<T, Integer> cluster() {
		Map<T, Integer> cluster = new HashMap<T, Integer>();
		for (T i : this) {
			if (!cluster.containsKey(i)) {
				cluster.put(i, 1);
			}
			else {
				cluster.put(i, cluster.get(i) + 1);
			}
		}
		return cluster;
	}
	
	/**
	 * Calculate the sample variance of collected values.
	 * @see http://en.wikipedia.org/wiki/Variance#Population_variance_and_sample_variance
	 * 
	 * @return
	 */
	public double variance() {
		
		double avg = this.avg();
		double var = 0;
		
		for (T e : this) {
			var += Math.pow(avg - e.doubleValue(), 2);
		}
		
		return 1.0/(double)(this.size()-1) * var;
	}
	
	/**
	 * Prints sample values of the distribution.
	 */
	public void print() {
		System.out.println("size: " + this.size());
		System.out.println("min: " + this.min());
		System.out.println("max: " + this.max());
		System.out.println("avg: " + Aggregate.r(this.avg(),4));
		System.out.println("median: " + Aggregate.r(this.median(),4));
		System.out.println("quartile: " + Aggregate.r(this.quantile(0.25),4));
		System.out.println("quantile(0.9): " + Aggregate.r(this.quantile(0.9),4));
		System.out.println("variance: " + Aggregate.r(this.variance(),4));
		System.out.println("std deviation: " + Aggregate.r(Math.sqrt(this.variance()),4));
	}
	

	/**
	 * Rounds a double with precision 0.000.
	 * 
	 * @param d the number to round
	 * @return
	 */
	public static double r(double d) {
		return r(d,3);
	}
	
	/**
	 * Rounds a double with precision 1/(10^prec), i.e., prec positions behind the decimal mark.
	 * 
	 * @param d the number to round
	 * @param prec the precision
	 * @return
	 */
	public static double r(double d, int prec) {
		double pow = Math.pow(10, prec);
		return Math.round(d*pow)/pow;
	}
		
	/**
	 * A simple example.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		Aggregate<Integer> a = new Aggregate<Integer>();
		int max = 100;
		for (int i=0; i<max;i++) {
			a.add((int)(Math.random()*max)+1);
		}
		a.print();
	}
	
}

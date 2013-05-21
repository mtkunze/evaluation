package de.uni_potsdam.hpi.bpt.util;

/**
 * Licensed under the MIT License for Open Source Software, 
 * <http://opensource.org/licenses/MIT>.
 * Copyright (c) 2013, Matthias Kunze. 
 *  
 * @author <mtkunze@gmail.com>
 *
 */
public class Combinatorics {

	public static long binomialCoefficient(int n, int k) {
        long bin = 1;
        
        int m = n - k;
        if (k < m) {
            k = m;
        }
        
        for (int i = n, j = 1; i > k; i--, j++) {
            bin = bin * i / j;
        }
        
        return bin;
    }

}

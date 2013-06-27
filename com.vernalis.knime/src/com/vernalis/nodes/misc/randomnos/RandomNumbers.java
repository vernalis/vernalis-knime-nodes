/*
 * ------------------------------------------------------------------------
 *  Copyright (C) 2013, Vernalis (R&D) Ltd
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 * ------------------------------------------------------------------------
 */
package com.vernalis.nodes.misc.randomnos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class RandomNumbers {
	/*
	 * Some helper functions to return either a set or list of random ints or Doubles
	 */
	public static Collection<Integer> getUniqueInts (int min, int max, int n){


		//Check the range is sensible
		if (min>max){
			int t = min;
			min = max;
			max = t;
		}


		//Ensure that we dont get stuck in an infinite effort to create more integers than are in
		//the min max range
		if (n>(max-min+1)){
			n = max-min+1;
		}


		//Now populate the set
		Set<Integer> Numbers = new LinkedHashSet<Integer>();
		Random rand = new Random();
		Integer RandomNum;
		while (Numbers.size()<n){
			RandomNum=(Integer)(rand.nextInt(max - min + 1) + min);
			Numbers.add(RandomNum);
		}
		return Numbers;
	}


	public static Collection<Integer> getInts (int min, int max, int n){


		//Check the range is sensible
		if (min>max){
			int t = min;
			min = max;
			max = t;
		}



		//Now populate the set
		List<Integer> Numbers = new ArrayList<Integer>();
		Random rand = new Random();
		Integer RandomNum;
		while (Numbers.size()<n){
			RandomNum=(Integer)(rand.nextInt(max - min + 1) + min);
			Numbers.add(RandomNum);
		}
		return Numbers;
	}
	
	public static Collection<Double> getUniqueDoubles (Double min, Double max, int n){


		//Check the range is sensible
		if (min>max){
			Double t = min;
			min = max;
			max = t;
		}


		//Now populate the set
		Set<Double> Numbers = new LinkedHashSet<Double>();
		Double RandomNum;
		while (Numbers.size()<n){
			RandomNum= min + (Math.random() * ((max - min) + 1));
			Numbers.add(RandomNum);
		}
		return Numbers;
	}


	public static Collection<Double> getDoubles (Double min, Double max, int n){


		//Check the range is sensible
		if (min>max){
			Double t = min;
			min = max;
			max = t;
		}


		//Now populate the set
		List<Double> Numbers = new ArrayList<Double>();
		Double RandomNum;
		while (Numbers.size()<n){
			RandomNum= min + (Math.random() * ((max - min) + 1));
			Numbers.add(RandomNum);
		}
		return Numbers;
	}
	
	
	
}
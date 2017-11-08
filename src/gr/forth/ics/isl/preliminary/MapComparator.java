/*
This code belongs to the Semantic Access and Retrieval (SAR) group of the 
Information Systems Laboratory (ISL) of the 
Institute of Computer Science (ICS) of the  
Foundation for Research and Technology – Hellas (FORTH)

Nobody is allowed to use, copy, distribute, or modify this work.
It is published for reasons of research results reproducibility.

© 2017, Semantic Access and Retrieval group, All rights reserved

 */
package gr.forth.ics.isl.preliminary;


import java.util.Comparator;
import java.util.Map;


public class MapComparator implements Comparator<Prefix> {

Map<Prefix,Integer> base; 
public MapComparator(Map<Prefix, Integer> base) 
{ this.base = base; }

public int compare(Prefix a, Prefix b) { 
	if (base.get(a) <= base.get(b)) { 
		return 1; 
		} 
	else { 
		return -1; 
	} 
	}
}
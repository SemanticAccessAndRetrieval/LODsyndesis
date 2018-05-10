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
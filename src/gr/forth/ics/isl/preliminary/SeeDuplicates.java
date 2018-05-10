package gr.forth.ics.isl.preliminary;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class SeeDuplicates {
	public int id;
	int uris;
	int numberOfURIs;
	String name;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	Set<Integer> idsSet=new HashSet<Integer>();
	public int getNumberOfURIs() {
		return numberOfURIs;
	}
	public void setNumberOfURIs(int numberOfURIs) {
		this.numberOfURIs = numberOfURIs;
	}
	public int getUris() {
		return uris;
	}
	public void setUris(int uris) {
		this.uris = uris;
	}
	public String getIds() {
		return ids;
	}
	public void setIds(String ids) {
		this.ids = ids;
		String[] split=ids.split(",");
		this.setNumberOfURIs(split.length);
	}
	String ids;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public void incrementURIs(){
		this.uris = uris+1;
	}
	public void addID(int did){
		idsSet.add(did);
	}
	public String docIDs(){
		String idsret="";
		for(int x:idsSet){
			idsret=x+",";
		}
		idsret=idsret.substring(0,idsret.length()-1);
		return idsret;
	}
}

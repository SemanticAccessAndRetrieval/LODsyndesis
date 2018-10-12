/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.forth.ics.isl.lattice;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

class TrieNode {
    String c;
    Set<String> noVisit=new HashSet<String>();
    int n;
    Set<String> backNodes=new HashSet<String>();
    Set<String> contains=new HashSet<String>();
    public Set<String> getBackNodes() {
        return backNodes;
    }
    HashMap<String,Set<TrieNode>> transitive=new HashMap<>();
    
    HashMap<String,Set<TrieNode>> notNodes=new HashMap<>();
    HashMap<String,Integer> transitiveScore=new HashMap<>();
    public Set<String> getNoVisit() {
        return noVisit;
    }

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }

    public HashMap<String, Set<TrieNode>> getTransitive() {
        return transitive;
    }

    public void setTransitive(HashMap<String, Set<TrieNode>> transitive) {
        this.transitive = transitive;
    }

   
    
    public void setNoVisit(String noV) {
        this.noVisit.add(noV);
    }
    
    HashMap<String, TrieNode> children = new HashMap<String, TrieNode>();
    boolean isLeaf;
    int score=0;
    int id=0;
    int max=0;
    public TrieNode() {}
 
   
    public void setScore(int k){
        score=score+k;
    }
   
    
    public TrieNode(String c){
        this.c = c;
        id=Integer.parseInt(c);
    }
}
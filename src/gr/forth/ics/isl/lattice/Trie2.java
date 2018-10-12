/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.forth.ics.isl.lattice;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author micha
 */

public class Trie2 {
    private TrieNode root;
    int count=0,countRecursion=0;
    int wholeScore=0;
    int paths=0;
 
     Map<String,Set<TrieNode>> startsWith = new HashMap<>();
    Set<TrieNode> leavesSet=new HashSet<>();
    Set<TrieNode> nextNodes=new HashSet<>();
     public Set<TrieNode> getLeavesSet(){
        return leavesSet;
    }
    public Trie2() {
        root = new TrieNode();
        root.c="";
    }
    
    public Map<String, Set<TrieNode>> getStartsWith() {
        return startsWith;
    }

    public int getWholeScore() {
        return wholeScore;
    }

    public  int getCount() {
        return count;
    }

    public void setWholeScore(int wholeScore) {
        this.wholeScore = wholeScore;
    }
 
    public int getPaths(){
        return paths;
    }
    
    
    // Inserts a word into the trie.
    public void insert(String word1,int score,int k,int max) {
        HashMap<String, TrieNode> children = root.children;
        String[] word=word1.split(",");
        
        for(int i=0; i<word.length; i++){
            if(Integer.parseInt(word[i])<k || (Integer.parseInt(word[i])>max))
                continue;
            String c = word[i];
            TrieNode t;
            if(children.containsKey(c)){
                    t = children.get(c);
                    if (t.isLeaf == true) {
                    t.isLeaf = false;
                    // count--;
                }
            }else{
                t = new TrieNode(c);
                count++;
                children.put(c, t);
                
            }
            t.setScore(score);
            children = t.children;
             for(int j=i+1; j<word.length; j++){
                  if(Integer.parseInt(word[j])<k || (Integer.parseInt(word[j])>max))
                      continue;
                    t.contains.add(word[j]);
              }
            if(Integer.parseInt(word[word.length-1])>t.max)
                t.max=Integer.parseInt(word[word.length-1]);
               if (i == word.length - 1 && t.children.isEmpty()) {
                    t.isLeaf = true;

                 //leavesSet.add(t);
                 //t.setScore(score);
                 return;
            }
            //set leaf node
          //  if(i==word.length-1)
            //    t.isLeaf = true;    
        }
    }
 
    // Returns if the word is in the trie.
    public boolean search(String word) {
        TrieNode t = searchNode(word);
 
        if(t != null && t.isLeaf) 
            return true;
        else
            return false;
    }
 
    // Returns if there is any word in the trie
    // that starts with the given prefix.
    public boolean startsWith(String prefix) {
        if(searchNode(prefix) == null) 
            return false;
        else
            return true;
    }
 
    public TrieNode searchNode(String str){
        Map<String, TrieNode> children = root.children; 
        TrieNode t = null;
        String [] str1=str.split(",");
        for(int i=0; i<str1.length; i++){
            String c = str1[i];
            if(children.containsKey(c)){
                t = children.get(c);
                children = t.children;
            }else{
                return null;
            }
        }
 
        return t;
    }
    
      public void searchAll(TrieNode roo,boolean flag,int level,Set<String> constraints){
        Map<String, TrieNode> children;
        if(flag==true){
//             for(int i=0;i<level;i++)
//                System.out.print("\t");
//            System.out.println(root.c);
            level++;
            if(root.c==null || constraints.contains(root.c))
                return;
            children= root.children; 
        }
        else{
//             for(int i=0;i<level;i++)
//                System.out.print("\t");
//            System.out.println(roo.c);
            level++;
            if(roo.c==null || constraints.contains(roo.c))
                return;
            children= roo.children; 
        }
        if(children.isEmpty()){
            wholeScore+=roo.score;
           // System.out.println("Leaf: "+roo.score);
        }
          //System.out.println(children.keySet());
        for(String k :children.keySet()){
//           for(int i=0;i<level;i++)
//                System.out.print("\t");
//            System.out.println(k);
            if(constraints.contains(k))
                continue;
            if(!children.get(k).isLeaf){
                    for(String k1 :children.get(k).children.keySet()){
                    //    System.out.println(k1);
                        searchAll(children.get(k).children.get(k1),false,level+1,constraints);
                    }
            }
            else{
                //System.out.println(roo.score);
                // for(String k1 :children.get(k).children.keySet()){
                wholeScore+=children.get(k).score;
                    //System.out.println("Leaf: "+children.get(k).score);
                 //}
            }
            }
        }
        
      
      public void searchIntersection(TrieNode roo,boolean flag,int level,Set<String> constraints){
        Map<String, TrieNode> children;
        if(flag==true){
//             for(int i=0;i<level;i++)
//                System.out.print("\t");
//            System.out.println(root.c);
            level++;
            if(root.c==null || constraints.contains(root.c))
                return;
            children= root.children; 
        }
        else{
//             for(int i=0;i<level;i++)
//                System.out.print("\t");
//            System.out.println(roo.c);
            level++;
            if(roo.c==null || constraints.contains(roo.c))
                return;
            children= roo.children; 
        }
        if(children.isEmpty()){
            wholeScore+=roo.score;
           // System.out.println("Leaf: "+roo.score);
        }
          //System.out.println(children.keySet());
        for(String k :children.keySet()){
//           for(int i=0;i<level;i++)
//                System.out.print("\t");
//            System.out.println(k);
            if(constraints.contains(k))
                continue;
            if(!children.get(k).isLeaf){
                    for(String k1 :children.get(k).children.keySet()){
                    //    System.out.println(k1);
                        searchAll(children.get(k).children.get(k1),false,level+1,constraints);
                    }
            }
            else{
                //System.out.println(roo.score);
                // for(String k1 :children.get(k).children.keySet()){
                wholeScore+=children.get(k).score;
                    //System.out.println("Leaf: "+children.get(k).score);
                 //}
            }
            }
        }

    public Set<TrieNode> getNextNodes() {
        return nextNodes;
    }

    public void setNextNodes(Set<TrieNode> nextNodes) {
        this.nextNodes = nextNodes;
    }
    
      
      
        public void searchAndNext(TrieNode roo, int node){
        Map<String, TrieNode> children;
        countRecursion++;
        if(roo==null){
            children= root.children; 
        }
        else{
            if(roo.id==node){
                wholeScore+=roo.score;
                return;
            }
            
            else if(roo.id>node){
                 this.nextNodes.add(roo);
                 return;
            } 
            
           // else if (roo.isLeaf)// || roo.max<node)
          //      return;
            children= roo.children; 
        }
        for(String k :children.keySet()){
                      searchAndNext(children.get(k),node);
            }
        }
      
      
      



   
}

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

public class TrieInt {

    public TrieNode root;
    int count = 0,countRecursion=0;
    int trans=0;
    Set<TrieNode> nextNodes=new HashSet<>();
    Set<String> successPaths = new HashSet<String>();
    
    public Map<String, Set<TrieNode>> getStartsWith() {
        return startsWith;
    }
    
    Map<String,Set<TrieNode>> startsWith = new HashMap<>();
    
    public int getTrans() {
        return trans;
    }
    
    public void setTrans(int trans) {
        this.trans = trans;
    }
    Set<TrieNode> tn = new HashSet<TrieNode>();
    Set<TrieNode> leaves = new HashSet<TrieNode>();
    int wholeScore = 0;
    HashMap<String, Set<TrieNode>> tr = new HashMap<>();
    

    public TrieInt() {
        root = new TrieNode();
        root.c = "";
    }

    public TrieInt(TrieInt ti) {
        root = ti.root;
        root.c = "";
    }

    public int getWholeScore() {
        return wholeScore;
    }

    public int getCount() {
        return count;
    }

    public void setWholeScore(int wholeScore) {
        this.wholeScore = wholeScore;
    }

    // Inserts a word into the trie.
    public void insert(String word1, int score, int cn) {
        HashMap<String, TrieNode> children = root.children;
        String[] word = word1.split(",");
        Set<TrieNode> previous = new HashSet<>();
       previous.add(root);
        for (int i = 0; i < word.length; i++) {
            String c = word[i];
            
            if (c.equals("") || Integer.parseInt(c) < cn) {
                continue;
            }
            TrieNode t;
            if (children.containsKey(c)) {
                t = children.get(c);
                t.setScore(score);
                if (t.isLeaf == true) {
                    t.isLeaf = false;
                    // count--;
                }
            } else {
                count++;
                t = new TrieNode(c);
                if(startsWith.containsKey(c)){
                    startsWith.get(c).add(t);
                }
                else{
                    Set<TrieNode> trnodes=new HashSet<>();
                    trnodes.add(t);
                    startsWith.put(c,trnodes);
                }
                t.setN(Integer.parseInt(c));
                if (tr.containsKey(c)) {
                    tr.get(c).add(t);
                } else {
                    Set<TrieNode> tr1 = new HashSet<TrieNode>();
                    tr1.add(t);
                    tr.put(c, tr1);

                }
                t.setScore(score);
                children.put(c, t);
            }
            
            for (TrieNode pre : previous) {
                if (pre.getTransitive().containsKey(c)){
                   if(!pre.getTransitive().get(c).contains(t)) {
                    pre.getTransitive().get(c).add(t);
                    trans++;
                  }
                } else {
                    Set<TrieNode> tr1 = new HashSet<TrieNode>();
                    tr1.add(t);
                    pre.getTransitive().put(c, tr1);
                    pre.transitiveScore.put(c,0); 
                    trans++;
                }
                   pre.transitiveScore.put(c,pre.transitiveScore.get(c)+score);
            }
            previous.add(t);
            children = t.children;
            if (i == word.length - 1 && !t.children.isEmpty()) {
                t.isLeaf = true;
                //count++;
                //t.setScore(score);
                return;
            }
            //set leaf node
            //  if(i==word.length-1)
            //    t.isLeaf = true;    
        }
    }

    public HashMap<String, Set<TrieNode>> getTr() {
        return tr;
    }

    public void setTr(HashMap<String, Set<TrieNode>> tr) {
        this.tr = tr;
    }

    // Returns if the word is in the trie.
    public boolean search(String word) {
        TrieNode t = searchNode(word);

        if (t != null && t.isLeaf) {
            return true;
        } else {
            return false;
        }
    }

    // Returns if there is any word in the trie
    // that starts with the given prefix.
    public boolean startsWith(String prefix) {
        if (searchNode(prefix) == null) {
            return false;
        } else {
            return true;
        }
    }

    public TrieNode searchNode(String str) {
        Map<String, TrieNode> children = root.children;
        TrieNode t = null;
        String[] str1 = str.split(",");
        for (int i = 0; i < str1.length; i++) {
            String c = str1[i];
            if (children.containsKey(c)) {
                t = children.get(c);
                children = t.children;
            } else {
                return null;
            }
        }

        return t;
    }

    public boolean searchAll(TrieNode roo, boolean flag, int level, String node) {
        Map<String, TrieNode> children;
        if (flag == true) {
            for (int i = 0; i < level; i++) {
                System.out.print("\t");
            }
            System.out.println(root.c);
            if (root.c.equals(node)) {
                System.out.println("yes");
            }
            if (Integer.parseInt(root.c) == Integer.parseInt(node)) {
                wholeScore += root.score;
                return true;
            }
            if (Integer.parseInt(root.c) > Integer.parseInt(node)) {
                return false;
            }
            level++;
            children = root.children;
        } else {
            for (int i = 0; i < level; i++) {
                System.out.print("\t");
            }
            System.out.println(roo.c);
            if (roo.c.equals(node)) {
                System.out.println("yes");
            }
            level++;
            if (Integer.parseInt(roo.c) == Integer.parseInt(node)) {
                return true;
            }
            if (Integer.parseInt(roo.c) > Integer.parseInt(node)) {
                return false;
            }
            children = roo.children;
        }
        if (children.isEmpty()) {
            // System.out.println("Leaf: "+roo.score);
        }

        //System.out.println(children.keySet());
        for (String k : children.keySet()) {
            for (int i = 0; i < level; i++) {
                System.out.print(" ");
            }
            System.out.println(k);
            if (Integer.parseInt(k) == Integer.parseInt(node)) {
                return true;
            }
            if (Integer.parseInt(k) > Integer.parseInt(node)) {
                return false;
            }
            if (!children.get(k).isLeaf) {
                for (String k1 : children.get(k).children.keySet()) {
                    boolean decision = searchAll(children.get(k).children.get(k1), false, level + 1, node);
                    System.out.println(decision);
                    return decision;
                }
            } else {

                //System.out.println(roo.score);
                // for(String k1 :children.get(k).children.keySet()){
                wholeScore += children.get(k).score;
                //System.out.println("Leaf: "+children.get(k).score);
                //}
            }
        }
        return false;
    }

    public void printAll(TrieNode roo, boolean flag, int level, String node) {
        Map<String, TrieNode> children;
        if (flag == true) {
            for (int i = 0; i < level; i++) {
                System.out.print(" ");
            }
            // System.out.println(root.c);
            level++;
            children = root.children;
        } else {
            for (int i = 0; i < level; i++) {
                System.out.print(" ");
            }
            System.out.println(roo.c+" "+roo.score);
            if (roo.c.equals(node)) {
                System.out.println("yes");
            }
            level++;
            children = roo.children;
        }
        if (children.isEmpty()) {
            count++;
//            wholeScore+=roo.score;
            // System.out.println("Leaf: "+roo.score);
        }
        //  System.out.println(children.keySet());
        //System.out.println(children.keySet());
        for (String k : children.keySet()) {

            //  if(!children.get(k).isLeaf){
            // for(String k1 :children.get(k).children.keySet()){
            printAll(children.get(k), false, level + 1, node);
            //}
            // }
            //  else{

            //System.out.println(roo.score);
            // for(String k1 :children.get(k).children.keySet()){
            //    wholeScore+=children.get(k).score;
            //System.out.println("Leaf: "+children.get(k).score);
            //}
            // }
        }
    }

    public Set<TrieNode> getTrieNodes() {
        return this.tn;
    }

    public boolean removeTrie(TrieNode roo, boolean flag, int level, int node) {

        Map<String, TrieNode> children;
        if (flag == true) {
            children = root.children;
            if (!root.c.equals("")) {
                if (root.c.equals(node)) {

                    wholeScore += root.score;
                    return true;
                }
            }
        } else {

            if (roo.getN() == node) {

                wholeScore += roo.score;
                this.tn.add(roo);
                //successPaths.add(path);
                // System.out.println(path);

                return true;

            } else if (roo.n > node) {
                return false;
            }
            children = roo.children;
        }
        //boolean finalDecision=false;
        for (String k : children.keySet()) {
            removeTrie(children.get(k), false, level + 1, node);
        }

        return true;
    }
    
      public Set<TrieNode> getNextNodes() {
        return nextNodes;
    }

    public void setNextNodes(Set<TrieNode> nextNodes) {
        this.nextNodes = nextNodes;
    }
    
    
        public void searchAndNext(TrieNode roo, int node){
        Map<String, TrieNode> children;
        
        if(roo==null){
            children= root.children; 
        }
        else{
            countRecursion++;
            if(roo.id==node){
                wholeScore+=roo.score;
                this.nextNodes.add(roo);
                return;
            }
            
            else if(roo.id>node){
                // this.nextNodes.add(roo);
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

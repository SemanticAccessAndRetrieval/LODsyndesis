/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.forth.ics.isl.lattice;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author micha
 */
public class TopDown {
    TreeMap<Integer,Set<String>> ranking=new TreeMap<>();
    int maxLevel = 10;
    private Map<Integer, TreeSet<String>> subsets;
    int dcID = 0;
    int nodes = 0, loops, checks;
    private HashMap<String, Integer> directCountID;
    private Map<Integer, HashMap<String, HashSet<Integer>>> subsets2;
    BufferedWriter bw;
    Map<String, Map<String, Integer>> subse;

  
    Map<String, Trie2> tries2 = new HashMap<String, Trie2>();
    Map<String, TrieInt> triesInt = new HashMap<String, TrieInt>();

    public Map<String, Map<String, Integer>> getSubse() {
        return subse;
    }


    public void setSubse(String fName, String node) throws IOException {
        this.subse = this.setDirectCountsUnion(fName, node);
    }

    public HashMap<Integer, Integer> setDirectCountsBottomUp(String fName) throws FileNotFoundException, IOException {
        BufferedReader br = null;
        maxLevel = 0;
        String s;
        br = new BufferedReader(new FileReader(fName));
        int count = 1, zeros = 0;
        Map<Integer, HashMap<String, HashSet<Integer>>> subsets = new HashMap<Integer, HashMap<String, HashSet<Integer>>>();
        int splitCount = 1;
        File file;// = new File("split"+splitCount+".txt");
        FileWriter fw;// = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw;// = new BufferedWriter(fw);
        HashMap<Integer, Integer> ret = new HashMap<Integer, Integer>();
        HashMap<String, Integer> directCountID = new HashMap<String, Integer>();
        while ((s = br.readLine()) != null) {
            String[] p = s.split("\t");
            String[] line = p[0].split(",");

            String b = p[0]; //b.substring(0, b.length() - 1);
            if (!subsets.containsKey(line.length)) {
                subsets.put(line.length, new HashMap<String, HashSet<Integer>>());
            }
            if (!subsets.get(line.length).containsKey(b)) {
                subsets.get(line.length).put(b, new HashSet<Integer>());
            }
            if (!directCountID.containsKey(b)) {
                splitCount++;
                directCountID.put(b, dcID);
                ret.put(dcID, Integer.parseInt(p[1]));
                dcID++;
            } else {
                ret.put(directCountID.get(b), ret.get(directCountID.get(b)) + Integer.parseInt(p[1]));
                //directCountID.put(b, ret.get(b) + 1);
            }
            if (line.length > maxLevel) {
                maxLevel = line.length;
                System.out.println(s);
            }
            //}
        }
        System.out.println("Split Count: " + splitCount);
        this.setDirectCountID(directCountID);
        this.setSubsets2(subsets);
        return ret;
    }

    public void topDownMethod(String fName) throws IOException {
        HashMap<Integer, Integer> array = this.setDirectCountsBottomUp(fName);
        Map<Integer, HashMap<String, HashSet<Integer>>> subsetsD = this.subsets2;
        int level = maxLevel;
        // System.out.println("Level:"+level);
        int subsetsNum = 0, allDescs = 0;
        Map<String, Set<Integer>> descendants = new HashMap<>();
        FileWriter fw = null;
        BufferedWriter bw = null;
        int nodesNum = 0;

        while (level > 1) {
            //System.out.println(level);
            HashMap<String, HashSet<Integer>> levelB = subsetsD.get(level);
            if (!subsetsD.containsKey(level - 1)) {
                subsetsD.put(level - 1, new HashMap<String, HashSet<Integer>>());
            }

            int aa = 0;
            int descs = 0;
            for (Iterator<String> it = levelB.keySet().iterator(); it.hasNext();) {
                String node = it.next();
                subsetsNum++;
                if (this.directCountID.containsKey(node)) {
                    levelB.get(node).add(this.directCountID.get(node));
                }

                descs += levelB.get(node).size();
                int score = 0;
                for (int subset : levelB.get(node)) {
                    score += array.get(subset);
                }
                System.out.println(node + " " + score);
                nodesNum++;
                String[] substs = node.split(",");
                HashMap<String, HashSet<Integer>> levelB1 = subsetsD.get(level - 1);
                for (int j = 0; j < substs.length; j++) {
                    String sbs = "";
                    for (int i = 0; i < substs.length; i++) {
                        if (i != j) {
                            sbs += substs[i] + ",";
                        }

                    }
                    sbs = sbs.substring(0, sbs.length() - 1);
                    if (!levelB1.containsKey(sbs)) {
                        levelB1.put(sbs, new HashSet<Integer>());
                    }
                    levelB1.get(sbs).addAll(levelB.get(node));

                }
                it.remove();

            }
            subsetsD.get(level).clear();
            subsetsD.remove(level);
            allDescs += descs;
            //System.out.println("======" + level + " " + nodesNum + " Descs:" + descs);
            String value = "";
            String maxs = " ";
            level--;

        }
        System.out.println("All Subsets: " + subsetsNum + "All Desks:" + allDescs);

    }

    private void setDirectCountID(HashMap<String, Integer> directCountID) {
        this.directCountID = directCountID; //To change body of generated methods, choose Tools | Templates.
    }

    private void setSubsets2(Map<Integer, HashMap<String, HashSet<Integer>>> subsets) {
        this.subsets2 = subsets;
    }


    public Map<Integer, Map<String, Map<String, Integer>>> setDirectCountsTopDown(String fName) throws FileNotFoundException, IOException {
        BufferedReader br = null;
        String sCurrentLine;
        br = new BufferedReader(new FileReader(fName));
        int count = 1, zeros = 0;
        Map<String, Map<String, Integer>> descendants = new HashMap<>();
        Map<Integer, Map<String, Map<String, Integer>>> subsets = new TreeMap<Integer, Map<String, Map<String, Integer>>>();
        Map<String, Integer> scanIndex = new TreeMap<String, Integer>();
        int splitCount = 1;
        File file;// = new File("split"+splitCount+".txt");
        FileWriter fw;// = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw;// = new BufferedWriter(fw);
        HashMap<String, Integer> ret = new HashMap<String, Integer>();
        subsets.put(2, new TreeMap<String, Map<String, Integer>>());
        Map<Integer, Integer> dcNum = new HashMap<Integer, Integer>();
        int countNumberOfUnique = 0;
        int loops = 0;
        while ((sCurrentLine = br.readLine()) != null) {
            //System.out.println(u);
            String[] line = sCurrentLine.split("\t")[0].split(",");
            for (int i = 0; i < line.length - 1; i++) {
                String word = line[i];
                String sub = "";
                int cc = 0;

                for (int j = i + 1; j < line.length; j++) {
                    word = line[i] + "," + line[j];
                    if (!subsets.get(2).containsKey(word)) {
                        subsets.get(2).put(word, new HashMap<String, Integer>());
                        // descendants.put(word, new HashMap<String, Integer>());
                    }
                    sub = word;
                    String rest = "";
                    //System.out.println(sub);
                    for (int k = j + 1; k < line.length; k++) {
                        rest += line[k];
                        if (k + 1 != line.length) {
                            rest += ",";
                        }
                    }
                    if (subsets.get(2).get(word).containsKey(rest)) {
                        subsets.get(2).get(word).put(rest, subsets.get(2).get(word).get(rest) + Integer.parseInt(sCurrentLine.split("\t")[1]));
                    } else {
                        subsets.get(2).get(word).put(rest, Integer.parseInt(sCurrentLine.split("\t")[1]));
                    }

                    cc++;
                    loops++;
                }

            }

        }
        System.out.println(loops);
        System.out.println(maxLevel);
        //this.setDescendantsOf2(descendants);
        //this.setSubsetsBup(subsets);
        return subsets;
    }




    public void print() {
        System.out.println(loops);
    }

    public Map<String, Map<String, Integer>> setDirectCountsUnion(String fName, String node) throws FileNotFoundException, IOException {

        BufferedReader br = null;
        String sCurrentLine;
        HashSet<String> triads = new HashSet<String>();
        br = new BufferedReader(new FileReader(fName));
        int count = 1, zeros = 0;
        Map<String, Map<String, Integer>> descendants = new HashMap<>();
        Map<String, Map<String, Integer>> subsets = new TreeMap<String, Map<String, Integer>>();
        Map<String, Integer> scanIndex = new TreeMap<String, Integer>();
        int splitCount = 1;
        File file;// = new File("split"+splitCount+".txt");
        FileWriter fw;// = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw;// = new BufferedWriter(fw);
        int countNumberOfUnique = 0;
        int loops = 0;
        while ((sCurrentLine = br.readLine()) != null) {
            //System.out.println(u);
            String[] line = sCurrentLine.split("\t")[0].split(",");
            for (int i = 0; i < line.length; i++) {
                String word = line[i];
                String sub = "";
                int cc = 0;
                sub = word;
                String rest = "";
                if (line[i].equals(node)) {
                    if (subsets.containsKey(word)) {
                        subsets.get(word).put(sCurrentLine.split("\t")[0], Integer.parseInt(sCurrentLine.split("\t")[1]));
                    } else {
                        TreeMap<String, Integer> tr = new TreeMap<>();
                        tr.put(sCurrentLine.split("\t")[0], Integer.parseInt(sCurrentLine.split("\t")[1]));
                        subsets.put(word, tr);

                    }
                } else if (!Arrays.asList(line).contains(node)) {
                    if (subsets.containsKey(word)) {
                        subsets.get(word).put(sCurrentLine.split("\t")[0], Integer.parseInt(sCurrentLine.split("\t")[1]));
                    } else {
                        TreeMap<String, Integer> tr = new TreeMap<>();
                        tr.put(sCurrentLine.split("\t")[0], Integer.parseInt(sCurrentLine.split("\t")[1]));
                        subsets.put(word, tr);

                    }
                }

            }

        }
        for (String k : subsets.keySet()) {
            System.out.println(k + "\t" + subsets.get(k).size());
        }
        System.out.println(triads.size());
        //subsets.remove("23");
        return subsets;
    }

    public Map<String, Map<String, Map<String, Integer>>> setDirectCountsUnionNew(String fName) throws FileNotFoundException, IOException {

        BufferedReader br = null;
        String sCurrentLine;
        br = new BufferedReader(new FileReader(fName));
        int count = 1, zeros = 0;
        Map<String, Map<String, Integer>> descendants = new HashMap<>();
        Map<String, Map<String, Map<String, Integer>>> subsets = new HashMap<String, Map<String, Map<String, Integer>>>();
        Map<String, Integer> scanIndex = new TreeMap<String, Integer>();
        int splitCount = 1;
        File file;// = new File("split"+splitCount+".txt");
        FileWriter fw;// = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw;// = new BufferedWriter(fw);
        int countNumberOfUnique = 0;
        int loops = 0;
        while ((sCurrentLine = br.readLine()) != null) {
            //System.out.println(u);
            String[] line = sCurrentLine.split("\t")[0].split(",");
            for (int i = 0; i < line.length; i++) {
                String word = line[i];
                String sub = "";
                int cc = 0;
                sub = word;
                String rest = "";

                if (subsets.containsKey(word)) {
                    if (subsets.get(word).containsKey(line[0])) {
                        subsets.get(word).get(line[0]).put(sCurrentLine.split("\t")[0], Integer.parseInt(sCurrentLine.split("\t")[1]));
                    } else {
                        TreeMap<String, Integer> tr = new TreeMap<>();
                        tr.put(sCurrentLine.split("\t")[0], Integer.parseInt(sCurrentLine.split("\t")[1]));

                        subsets.get(word).put(line[0], tr);

                    }
                    //subsets.get(word).put();
                } else {
                    TreeMap<String, Map<String, Integer>> newTr = new TreeMap<>();
                    TreeMap<String, Integer> tr = new TreeMap<>();
                    tr.put(sCurrentLine.split("\t")[0], Integer.parseInt(sCurrentLine.split("\t")[1]));
                    String startsWith = line[0];
                    newTr.put(startsWith, tr);
                    subsets.put(word, newTr);

                }
            }

        }
        for (String k : subsets.keySet()) {

            for (String k1 : subsets.get(k).keySet()) {
                // System.out.println("===="+k1);
                System.out.println(subsets.get(k).get(k1));
            }
            System.out.println(k + "\t" + subsets.get(k).size());
        }
        //subsets.remove("23");
        return subsets;
    }



  
    public void setTriesInt(String fname, int max) {
        try {
            int whole = 0, nodes1 = 0;

            for (int i = 0; i <= max; i++) {
                triesInt.put(Integer.toString(i), new TrieInt());
            }

            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(fname));
            } catch (FileNotFoundException ex) {
                Logger.getLogger(TopDown.class.getName()).log(Level.SEVERE, null, ex);
            }
            String s;

            HashMap<ArrayList<String>, Integer> ret = new HashMap<>();
            int sc = 0;
            HashMap<Integer, Integer> sum = new HashMap<>();
            int count = 0;
            while ((s = br.readLine()) != null) {
                //nodes1++;
                //if(nodes1>10000)
                //  return;
                String[] dtsetsA = s.split("\t")[0].split(",");
                String word = "";
                for (int j = dtsetsA.length - 1; j >= 0; j--) {
                    //System.out.println(j);
                    if (Integer.parseInt(dtsetsA[j]) <= max) {
                        if (!word.equals("")) {
                            word = dtsetsA[j] + "," + word;
                        } else {
                            word = dtsetsA[j] + "";
                        }
                        //System.out.println(dtsetsA[j]);;
                        triesInt.get(dtsetsA[j]).insert(word, Integer.parseInt(s.split("\t")[1]), Integer.parseInt(dtsetsA[j]));
                    }
                }

            }
//        
//            TrieInt tr = new TrieInt();
//            for (String subset : subse.get(k).keySet()) {
//
//                tr.insert(subset, subse.get(k).get(subset), Integer.parseInt(k));
//            }
            for (String k : triesInt.keySet()) {
                //  System.out.println(k + "\t" + triesInt.get(k).getCount() + "\t" + triesInt.get(k).getTrans());
                whole += triesInt.get(k).getTrans();
                // System.out.println(tr.count);
                //triesInt.put(k, tr);

                nodes1 += triesInt.get(k).getCount();
            }
            //System.out.println(nodes1);
            //System.out.println(whole);
        } catch (IOException ex) {
            Logger.getLogger(TopDown.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public TrieInt setOneTrie(String fname) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(fname));
            String s;
            TrieInt tr = new TrieInt();
            int sc = 0;
            HashMap<Integer, Integer> sum = new HashMap<>();
            while ((s = br.readLine()) != null) {
                // if(s.contains("6"))
                tr.insert(s.split("\t")[0], Integer.parseInt(s.split("\t")[1]), 0);

            }
            return tr;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TopDown.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TopDown.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                br.close();
            } catch (IOException ex) {
                Logger.getLogger(TopDown.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    public void setTrieUnion(String fname, int max) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(fname));
            String s;

            int sc = 0;
            HashMap<Integer, Integer> sum = new HashMap<>();

            for (int i = 0; i <= max; i++) {
                Trie2 tr = new Trie2();
                br = new BufferedReader(new FileReader(fname));
                while ((s = br.readLine()) != null) {
                    // if(s.contains("6"))
                    if (s.split("\t").length == 2) {
                        tr.insert(s.split("\t")[0], Integer.parseInt(s.split("\t")[1]), i, max);
                    }

                }
                tries2.put(Integer.toString(i), tr);
                //    sc += tr.count;
                //    System.out.println(i + " " + tr.count);
            }
            //System.out.println(tries2.get("1").count);
            //System.out.println("AVG:"+(double) sc/(double)max);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TopDown.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TopDown.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                br.close();
            } catch (IOException ex) {
                Logger.getLogger(TopDown.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public HashMap<ArrayList<String>, Integer> setUnionDC(String fname, int i, int max) {

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(fname));
            String s;

            HashMap<ArrayList<String>, Integer> ret = new HashMap<>();
            int sc = 0;
            HashMap<Integer, Integer> sum = new HashMap<>();
            int count = 0;
            while ((s = br.readLine()) != null) {
                // if(s.contains("6"))
                if (s.split("\t").length != 2) {
                    continue;
                }
                ArrayList<String> dtsets = new ArrayList<String>();
                for (String k : s.split("\t")[0].split(",")) {
                    if (Integer.parseInt(k) >= i && Integer.parseInt(k) <= max) {
                        dtsets.add(k);
                    }
                }

                if (dtsets.size() > 0 && Integer.parseInt(dtsets.get(dtsets.size() - 1)) >= i) {
                    if (ret.containsKey(dtsets)) {
                        ret.put(dtsets, ret.get(dtsets) + Integer.parseInt(s.split("\t")[1]));
                    } else {
                        ret.put(dtsets, Integer.parseInt(s.split("\t")[1]));
                    }
                }
                // dtsets.addAll(Arrays.asList(s.split("\t")[0].split(",")));
                // int last = Integer.parseInt(s.split("\t")[0].split(",")[s.split("\t")[0].split(",").length - 1]);
                //System.out.println(last+" "+i);
                //if (last >= i) {
                //    ret.put(dtsets, Integer.parseInt(s.split("\t")[1]));
                // }

            }
            count += ret.size();
            //   System.out.println(count);
            return ret;

        } catch (FileNotFoundException ex) {
            Logger.getLogger(TopDown.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TopDown.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                br.close();
            } catch (IOException ex) {
                Logger.getLogger(TopDown.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    public HashMap<Integer, HashMap<ArrayList<String>, Integer>> setUnionDCPruning(String fname, int max) {

        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(fname));
            String s;
            HashMap<Integer, HashMap<ArrayList<String>, Integer>> map = new HashMap<>();
            for (int t = 1; t <= max; t++) {
                map.put(t, new HashMap<>());
            }
            HashMap<ArrayList<String>, Integer> ret = new HashMap<>();
            int sc = 0;
            HashMap<Integer, Integer> sum = new HashMap<>();
            int count = 0;
            while ((s = br.readLine()) != null) {
                // if(s.contains("6"))
                if (s.split("\t").length != 2) {
                    continue;
                }
                String[] ids = s.split("\t")[0].split(",");
                ArrayList<String> dtsets = new ArrayList<String>();
                HashSet<Integer> already = new HashSet<>();
                int cmin = 0, cmax = max;
                for (int p = ids.length - 1; p >= 0; p--) {

                    if (Integer.parseInt(ids[p]) <= max) {
                        dtsets.add(ids[p]);
                        cmin = Integer.parseInt(ids[p]);
                        ArrayList<String> toAdd = new ArrayList<String>(dtsets);

                        if (p == 0) {
                            cmin = 1;
                        }

                        for (int i = cmin; i <= cmax; i++) {
                            
                            ArrayList<String> toAdd2 = new ArrayList<String>(toAdd);
                            if(i>Integer.parseInt(ids[p]))
                                toAdd2.remove(ids[p]);
                            if (toAdd2.size() > 0) {
                                if (map.get(i).containsKey(toAdd2)) {

                                    map.get(i).put(toAdd2, map.get(i).get(toAdd2) + Integer.parseInt(s.split("\t")[1]));
                                } else {
                                    map.get(i).put(toAdd2, Integer.parseInt(s.split("\t")[1]));
                                }

                            }

                        }

                        cmax = cmin - 1;
                    }

                    // dtsets.addAll(Arrays.asList(s.split("\t")[0].split(",")));
                    // int last = Integer.parseInt(s.split("\t")[0].split(",")[s.split("\t")[0].split(",").length - 1]);
                    //System.out.println(last+" "+i);
                    //if (last >= i) {
                    //    ret.put(dtsets, Integer.parseInt(s.split("\t")[1]));
                    // }
                }
            }
            count += ret.size();
            //   System.out.println(count);
            return map;

        } catch (FileNotFoundException ex) {
            Logger.getLogger(TopDown.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TopDown.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                br.close();
            } catch (IOException ex) {
                Logger.getLogger(TopDown.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    public HashMap<Integer, HashMap<ArrayList<String>, Integer>> setIntersectionDC(String fname, int max) {
        BufferedReader br = null;
        try {

            br = new BufferedReader(new FileReader(fname));
            String s;
            HashMap<Integer, HashMap<ArrayList<String>, Integer>> map = new HashMap<>();

            for (int t = 0; t <= max; t++) {
                map.put(t, new HashMap<>());
            }
            HashMap<ArrayList<String>, Integer> ret = new HashMap<>();
            int sc = 0;
            HashMap<Integer, Integer> sum = new HashMap<>();
            int count = 0;
            while ((s = br.readLine()) != null) {
                // if(s.contains("6"))
                ArrayList<String> dtsets = new ArrayList<String>();
                boolean flag = false;
                String[] ids = s.split("\t")[0].split(",");
                for (int p = ids.length - 1; p >= 0; p--) {
                    if (Integer.parseInt(ids[p]) <= max) {
                        dtsets.add(ids[p]);
                        ArrayList<String> toAdd = new ArrayList<String>(dtsets);

                        if (toAdd.size() > 0) {
                            if (map.get(Integer.parseInt(ids[p])).containsKey(toAdd)) {

                                map.get(Integer.parseInt(ids[p])).put(toAdd, map.get(Integer.parseInt(ids[p])).get(toAdd) + Integer.parseInt(s.split("\t")[1]));
                            } else {
                                map.get(Integer.parseInt(ids[p])).put(toAdd, Integer.parseInt(s.split("\t")[1]));
                            }

                        }
                    }

                }
                //if(flag==false)
                //  continue;

                // dtsets.addAll(Arrays.asList(s.split("\t")[0].split(",")));
                // int last = Integer.parseInt(s.split("\t")[0].split(",")[s.split("\t")[0].split(",").length - 1]);
                //System.out.println(last+" "+i);
                //if (last >= i) {
                //    ret.put(dtsets, Integer.parseInt(s.split("\t")[1]));
                // }
            }
            count += ret.size();
            // System.out.println(count);
            return map;

        } catch (FileNotFoundException ex) {
            Logger.getLogger(TopDown.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TopDown.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                br.close();
            } catch (IOException ex) {
                Logger.getLogger(TopDown.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

   

    public void recursiveUnionTrie(ArrayList<String> order, int level, String fName, String nod, int scoreAll, String newNode, String oldNode, Set<String> nodes2, Set<TrieNode> tr, Trie2 trie2, int maxB, boolean print) throws IOException {
        int max = maxB;
        if (level == 1) {
            this.setTrieUnion(fName, max);
            for (int j = 1; j <= max; j++) {
                String node = Integer.toString(j);
                //tries2.get("1").wholeScore=0;
                //System.out.println(tries2.get("1").);
                tries2.get(node).searchAndNext(null, j);
                HashSet<TrieNode> triej = new HashSet<TrieNode>(tries2.get(node).getNextNodes());
                int score = tries2.get(node).getWholeScore();
                //  System.out.println(node + " " + score);
                //if (print == true) {
                // System.out.println(node + " " + score);
                // }
                for (int i = Integer.parseInt(node) + 1; i <= max; i++) {
                    Set<String> nodes3 = new TreeSet<String>();
                    nodes3.add(node);
                    // if(order.contains(i))
                    //   continue;
                    String newN = node + "," + i;// order.get(i);
                    recursiveUnionTrie(order, 2, null, newN, score, Integer.toString(i), node, nodes3, triej, tries2.get(node), maxB, print);
                }
            }

        } else {
            nodes++;
            if (nodes % 10000 == 0) {
                  //  System.out.println(nodes);
            }
            level++;
            trie2.countRecursion = 0;
            int score = scoreAll;
            trie2.setWholeScore(0);
            trie2.nextNodes.clear();

            for (TrieNode tri : tr) {
                trie2.searchAndNext(tri, Integer.parseInt(newNode));
            }
            checks = checks + trie2.countRecursion;
            HashSet<TrieNode> triej = new HashSet<TrieNode>(trie2.getNextNodes());
            score += trie2.getWholeScore();
            //if(level==4)
              // return;
            if (print == true) {
                System.out.println(nod + " " + score);
            }
            for (int i = Integer.parseInt(newNode) + 1; i <= max; i++) {
                nodes2.add(newNode);
                String node = nod + "," + i;//order.get(i);
                recursiveUnionTrie(order, level, null, node, score, Integer.toString(i), nod, new HashSet(nodes2), triej, trie2, maxB, print);
            }

        }
    }

    public void recursiveSFIntersection(ArrayList<String> order, int level, String fName, String nod, int scoreAll, String newNode, String oldNode, Map<ArrayList<String>, Integer> ups, TrieInt tr, int maxB, boolean print) throws IOException, CloneNotSupportedException {
        int max = maxB;
        if (level == 2) {
            HashMap<Integer, HashMap<ArrayList<String>, Integer>> ups1 = this.setIntersectionDC(fName, max);
            for (int j = 1; j <= max; j++) {
                String node = Integer.toString(j);
                int score = 0;

                //System.out.println(node + " " + ups1.get(j));
                for (Iterator<Map.Entry<ArrayList<String>, Integer>> it = ups1.get(j).entrySet().iterator(); it.hasNext();) {

                    Map.Entry<ArrayList<String>, Integer> entry = it.next();
                    checks ++;//= entry.getKey().size();
                    //System.out.println(entry.getKey());
                    if (!entry.getKey().contains(node)) {
                        int cScore = entry.getValue();
                        it.remove();
                    } else {
                        int cScore = entry.getValue();
                        score += cScore;

                    }
                }
                //Map<ArrayList<String>, Integer> upsN = new HashMap(ups1.get(j));

                //System.out.println(node + " " + score);
                for (int i = j + 1; i <= max; i++) {
                    TrieInt tr1 = triesInt.get(Integer.toString(i - 1));
                    String newN = node + "," + i;
                    recursiveSFIntersection(order, 3, null, newN, score, Integer.toString(i), node, new HashMap(ups1.get(j)), tr1, maxB, print);
                }
            }
        } else {
            nodes++;

            if (nodes % 10000 == 0) {
                //System.out.println(nodes);
            }
            int score = 0;// scoreAll;
            boolean gBreak = false;
            Set<ArrayList<String>> remove = new HashSet<>();

            for (Iterator<Map.Entry<ArrayList<String>, Integer>> it = ups.entrySet().iterator(); it.hasNext();) {
                Map.Entry<ArrayList<String>, Integer> entry = it.next();
                checks++;//=entry.getKey().size();//++;
                if (!entry.getKey().contains(newNode)) {
                    int cScore = entry.getValue();
                    it.remove();
                } else {
                    int cScore = entry.getValue();
                    score += cScore;

                }

            }

//              for(Iterator<Map.Entry<ArrayList<String>,Integer>> it = ups.entrySet().iterator(); it.hasNext(); ) {
//                Map.Entry<ArrayList<String>,Integer> entry = it.next();
//              int cScore = entry.getValue();
//                         score += cScore;
//                
//              }
//            Map<ArrayList<String>, Integer> ups1 = new HashMap(ups);
            //if(tr.containsKey(
            if (print == true) {
                System.out.println(nod + " " + score);
            }
            //}
            //if(score<5)
            ///  return;
            //if (level == 4  || score == 0) {
            //   if(score>0)
            // System.out.println(nod + " " + score);
            //   return;
            //}

            level++;
            for (int i = Integer.parseInt(newNode) + 1; i <= max; i++) {
                String node = nod + "," + i;
                recursiveSFIntersection(order, level, null, node, score, Integer.toString(i), nod, new HashMap(ups), tr, maxB, print);

            }

        }
    }

    public void recursiveIntersectionTrie(ArrayList<String> order, int level, String fName, String nod, int scoreAll, String newNode, String oldNode, Map<ArrayList<String>, Integer> ups, TrieInt tr, Set<TrieNode> trieNodes, int maxB, boolean print) throws IOException, CloneNotSupportedException {
        int max = maxB;
        if (level == 1) {
            long startTime = System.currentTimeMillis();
            this.setTriesInt(fName, max);
            //this.setSubse(fName, "");

            //    Map<String, Map<String, Integer>> subsetsB = this.getSubse();
            for (int j = 1; j <= max; j++) {
                int score = 0;
                String node = Integer.toString(j);
                Map<ArrayList<String>, Integer> ups1 = new HashMap();
                //System.out.println(node + " " + score);
                TrieInt tr1 = triesInt.get(node);
                for (int i = Integer.parseInt(node) + 1; i <= max; i++) {

                    String newN = node + "," + i;
                    if (!tr1.getTr().containsKey(node)) {
                        continue;
                    }
                    recursiveIntersectionTrie(order, 2, null, newN, score, Integer.toString(i), node, ups1, tr1, tr1.getTr().get(node), maxB, print);
                }
                //System.out.println(nodes);
            }
        } else {
            nodes++;
            level++;
            if (nodes % 10000000 == 0) {
               //s  System.out.println(nodes);
            }
            int score = 0;// scoreAll;
            int prevNodes = 0, checksNow = 0;
            Set<TrieNode> trieN = new HashSet<TrieNode>();
            if (trieNodes != null) {
                for (TrieNode tn : trieNodes) { //{ 
                    checks++;
                    if (tn.getTransitive().containsKey(newNode)) {
                        for (TrieNode tn1 : tn.getTransitive().get(newNode)) {

                            checks++;
                            score += tn1.score;
                            if (!tn1.getTransitive().isEmpty()) {
                                trieN.add(tn1);
                            }
                            //tr.removeTrie(tn1, false, 0, Integer.parseInt(newNode));
                        }
                    }
                }
            }
            if (print == true) {
                System.out.println(nod + " " + score);
            }
            
            // int cScore = tr.wholeScore;
            // score += cScore;
            // HashSet<TrieNode> traverse = new HashSet<>(trieN);
//tr.getTrieNodes().clear();
            for (int i = Integer.parseInt(newNode) + 1; i <= max; i++) {
                String node = nod + "," + i;
                recursiveIntersectionTrie(order, level, null, node, score, Integer.toString(i), nod, null, tr, trieN, maxB, print);

            }

        }
    }

    public void recursiveIntersectionTrieNoPruning(ArrayList<String> order, int level, String fName, String nod, int scoreAll, String newNode, String oldNode, Map<ArrayList<String>, Integer> ups, TrieInt tr, Set<TrieNode> trieNodes, int maxB, boolean print) throws IOException, CloneNotSupportedException {
        int max = maxB;
        if (level == 1) {
            long startTime = System.currentTimeMillis();

            //this.setSubse(fName, "");
            Map<String, Map<String, Integer>> subsetsB = this.getSubse();
            this.setTriesIntNoPruning(fName, max);
            for (int j = 1; j <= max; j++) {
                int score = 0;
                String node = Integer.toString(j);
                Map<ArrayList<String>, Integer> ups1 = new HashMap();
                //System.out.println(node + " " + score);
                //  System.out.println(triesInt.get("1").getCount());
                // triesInt.clear();

                //
                TrieInt tr1 = triesInt.get("1");
                // System.out.println(tr1.getCount());
                for (int i = Integer.parseInt(node) + 1; i <= max; i++) {

                    String newN = node + "," + i;
                    //System.out.println(tr1.getTr().get(node).size());
                    if (!tr1.getTr().containsKey(node)) {
                        // System.out.println("hi");
                        continue;
                    }

                    recursiveIntersectionTrieNoPruning(order, 2, null, newN, score, Integer.toString(i), node, ups1, tr1, tr1.root.getTransitive().get(node), maxB, print);
                }
                //System.out.println(nodes);
            }
        } else {
            nodes++;
            level++;
            if (nodes % 100000 == 0) {
                 System.out.println(nodes);
            }
            int score = 0;// scoreAll;
            int prevNodes = 0, checksNow = 0;
            Set<TrieNode> trieN = new HashSet<TrieNode>();
            if (trieNodes != null) {
                for (TrieNode tn : trieNodes) { //{ 
                    checks++;
                    if (tn.getTransitive().containsKey(newNode)) {
                        for (TrieNode tn1 : tn.getTransitive().get(newNode)) {
                            checks++;
                            score += tn1.score;
                            if (!tn1.getTransitive().isEmpty()) {
                                trieN.add(tn1);
                            }
                            //tr.removeTrie(tn1, false, 0, Integer.parseInt(newNode));
                        }
                    }
                }
            }
            Random rand = new Random();
//            int n=0;
            //    int n = rand.nextInt(5)+1; //nod.split(",").length
            //System.out.println(nod+ " "+n);
            //    if(n==1)
            //   System.out.println(nod+"\t1");
            //System.out.println(nod+" "+prevNodes+" " +checksNow+" "+trieN.size());
//            if (score == 0 || level == 5) {
//              //  return;
//            }
            if (print == true) {
                System.out.println(nod + " " + score);
            }
           
            // int cScore = tr.wholeScore;
            // score += cScore;
            // HashSet<TrieNode> traverse = new HashSet<>(trieN);
//tr.getTrieNodes().clear();
            for (int i = Integer.parseInt(newNode) + 1; i <= max; i++) {
                String node = nod + "," + i;
                recursiveIntersectionTrieNoPruning(order, level, null, node, score, Integer.toString(i), nod, null, tr, trieN, maxB, print);

            }

        }
    }

    public void recursiveIntersectionTrieNoTrans(ArrayList<String> order, int level, String fName, String nod, int scoreAll, String newNode, String oldNode, Map<ArrayList<String>, Integer> ups, TrieInt tr, Set<TrieNode> trieNodes, int maxB, boolean print) throws IOException, CloneNotSupportedException {
        int max = maxB;
        if (level == 1) {
            long startTime = System.currentTimeMillis();

            //this.setSubse(fName, "");
            this.setTriesInt(fName, max);
            Map<String, Map<String, Integer>> subsetsB = this.getSubse();

            for (int j = 1; j <= max; j++) {
                int score = 0;
                String node = Integer.toString(j);
                Map<ArrayList<String>, Integer> ups1 = new HashMap();
                //System.out.println(node + " " + score);
                TrieInt tr1 = triesInt.get(node);
                for (int i = Integer.parseInt(node) + 1; i <= max; i++) {

                    String newN = node + "," + i;

                    recursiveIntersectionTrieNoTrans(order, 2, null, newN, score, Integer.toString(i), node, ups1, tr1, null, maxB, print);//tr1.getTr().get(node));
                }
            }
        } else {

            nodes++;
            level++;
            if (nodes % 10000 == 0) {
                //  System.out.println(nodes);
            }
            int score = 0;// scoreAll;

            tr.setWholeScore(0);
            tr.nextNodes.clear();
            tr.countRecursion = 0;
            if (trieNodes == null) {
                tr.searchAndNext(null, Integer.parseInt(newNode));
            } else {

                for (TrieNode tri : trieNodes) {
                    tr.searchAndNext(tri, Integer.parseInt(newNode));
                }
            }
            checks += tr.countRecursion;
            HashSet<TrieNode> triej = new HashSet<TrieNode>(tr.getNextNodes());
            score += tr.getWholeScore();
            if (print == true) {
                System.out.println(nod + " " + score);
            }
            //     if(level==4 || score==0)
            //       return;
            //    }
            //}
            // int cScore = tr.wholeScore;
            // score += cScore;
            // HashSet<TrieNode> traverse = new HashSet<>(trieN);
//tr.getTrieNodes().clear();
            for (int i = Integer.parseInt(newNode) + 1; i <= max; i++) {

                String node = nod + "," + i;
                recursiveIntersectionTrieNoTrans(order, level, null, node, score, Integer.toString(i), nod, null, tr, triej, maxB, print);

            }

        }
    }

    public void recursiveOneTrie(ArrayList<String> order, int level, String fName, String nod, int scoreAll, String newNode, String oldNode, Map<ArrayList<String>, Integer> ups, TrieInt tr, Set<TrieNode> trieNodes) throws IOException, CloneNotSupportedException {

        if (level == 1) {
            TrieInt trie = this.setOneTrie(fName);
            //Map<String, Map<String, Integer>> subsetsB = this.getSubse();
            for (int j = 0; j <= 21; j++) {
                String node = Integer.toString(j);
                // for (String node : subsetsB.keySet()) {
                //   int score = 0;
                //  Map<ArrayList<String>, Integer> ups1 = new HashMap();
                //System.out.println(node + " " + score);
                // TrieInt tr1 = triesInt.get(node);
                for (int i = j + 1; i <= 21; i++) {

                    String newN = j + "," + order.get(i);
                    recursiveOneTrie(order, 2, null, newN, 0, Integer.toString(i), node, null, trie, trie.getStartsWith().get(node));
                }
            }
        } else {

            nodes++;
            level++;
            if (nodes % 10000 == 0) {
                System.out.println(nodes);
            }
            int score = 0;// scoreAll;
            boolean gBreak = false;

            tr.setWholeScore(0);
//            if(trieNodes==null){
//                tr.removeTrie(null, true, 0, Integer.parseInt(newNode));
//            }
//            else{

            Set<TrieNode> trieN = new HashSet<TrieNode>();
            for (TrieNode tn : trieNodes) { //{
                if (tn.c.equals(newNode)) {
                    trieN.add(tn);
                    score += tn.score;

                    // tr.removeTrie(tn, false, 0, Integer.parseInt(newNode));
                } //System.out.println(tn.getTransitive());
                //System.out.println(tn.getTransitive().keySet());
                else if (tn.getTransitive().containsKey(newNode)) {

                    for (TrieNode tn1 : tn.getTransitive().get(newNode)) {
                        //
                        //if(nod.equals("6,8"))
                        // System.out.println(tn1.score);
                        // tr.printAll(tn1,false, level, nod);
                        // }
                        score += tn1.score;
                        trieN.add(tn1);
                        //tr.removeTrie(tn1, false, 0, Integer.parseInt(newNode));
                    }
                }
            }
            //    }
            //}
            //    System.out.println(nod + " " + score);
            // int cScore = tr.wholeScore;
            // score += cScore;

            HashSet<TrieNode> traverse = new HashSet<>(trieN);

//tr.getTrieNodes().clear();
            for (int i = Integer.parseInt(newNode) + 1; i <= 21; i++) {

                String node = nod + "," + order.get(i);
                recursiveOneTrie(order, level, null, node, score, Integer.toString(i), nod, null, tr, traverse);

            }

        }
    }


    public void recursiveSFUnion(ArrayList<String> order, int level, String fName, String nod, int scoreAll, String newNode, String oldNode, Map<ArrayList<String>, Integer> ups, int maxB, boolean print) throws IOException, CloneNotSupportedException {
        int max = maxB;
        if (level == 2) {
            HashMap<Integer, HashMap<ArrayList<String>, Integer>> ups2 = this.setUnionDCPruning(fName, max);
            for (int j = 1; j <= max; j++) {
                String node = Integer.toString(j);
                int score = 0;

                Map<ArrayList<String>, Integer> ups1 = new HashMap(ups2.get(j));
               // System.out.println(j+" "+ups1);
                for (Iterator<Map.Entry<ArrayList<String>, Integer>> it = ups1.entrySet().iterator(); it.hasNext();) {
                    this.checks++;
                    Map.Entry<ArrayList<String>, Integer> entry = it.next();
                    
                    if (entry.getKey().contains(node)) {
                        int cScore = entry.getValue();
                        score += cScore;
                        it.remove();
                    }
                }
                
                if (print == true) {
                    // System.out.println(node + " " + score);
                }
               //  Map<ArrayList<String>, Integer> upsN = new HashMap(ups1);
                for (int i = j + 1; i <= max; i++) {
                    String newN = node + "," + i;
                   
                    recursiveSFUnion(order, 3, null, newN, score, Integer.toString(i), node, new HashMap(ups1), maxB, print);
                }
            }
        } else {

            nodes++;
            level++;
            if (nodes % 10000 == 0) {
             //    System.out.println(nodes);
            }
            int score = scoreAll;
            boolean gBreak = false;
            Set<ArrayList<String>> remove = new HashSet<>();

            for (Iterator<Map.Entry<ArrayList<String>, Integer>> it = ups.entrySet().iterator(); it.hasNext();) {
                Map.Entry<ArrayList<String>, Integer> entry = it.next();
                this.checks++;
                if (entry.getKey().contains(newNode)) {
                    
                    int cScore = entry.getValue();
                    score += cScore;
                    it.remove();
                } else if (Integer.parseInt(entry.getKey().get(0)) < Integer.parseInt(newNode)) {
                    
                    // System.out.println(Integer.parseInt(entry.getKey().get(entry.getKey().size()-1))+" "+newNode);
                   it.remove();

                }

            }
            Map<ArrayList<String>, Integer> ups1 = new HashMap(ups);
            if (print == true) {
                System.out.println(nod + " " + score);
            }
           
            for (int i = Integer.parseInt(newNode) + 1; i <= max; i++) {
                String node = nod + "," + i;
                recursiveSFUnion(order, level, null, node, score, Integer.toString(i), nod, new HashMap(ups), maxB, print);

            }

        }
    }

   


    public void setTriesIntNoPruning(String fname, int max) {
        try {
            int whole = 0, nodes1 = 0;

            for (int i = 0; i <= max; i++) {
                triesInt.put(Integer.toString(i), new TrieInt());
            }

            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(fname));
            } catch (FileNotFoundException ex) {
                Logger.getLogger(TopDown.class.getName()).log(Level.SEVERE, null, ex);
            }
            String s;

            HashMap<ArrayList<String>, Integer> ret = new HashMap<>();
            int sc = 0;
            HashMap<Integer, Integer> sum = new HashMap<>();
            int count = 0;
            while ((s = br.readLine()) != null) {
                //nodes1++;
                //if(nodes1>10000)
                //  return;
                String[] dtsetsA = s.split("\t")[0].split(",");
                String word = "";
                for (int j = dtsetsA.length - 1; j >= 0; j--) {
                    //System.out.println(j);
                    if (Integer.parseInt(dtsetsA[j]) <= max) {
                        if (!word.equals("")) {
                            word = dtsetsA[j] + "," + word;
                        } else {
                            word = dtsetsA[j] + "";
                        }
                        //System.out.println(dtsetsA[j]);;

                    }
                }
                triesInt.get("1").insert(word, Integer.parseInt(s.split("\t")[1]), 1);

            }
//        
//            TrieInt tr = new TrieInt();
//            for (String subset : subse.get(k).keySet()) {
//
//                tr.insert(subset, subse.get(k).get(subset), Integer.parseInt(k));
//            }
            for (String k : triesInt.keySet()) {
               //   System.out.println(k + "\t" + triesInt.get(k).getCount() + "\t" + triesInt.get(k).getTrans());
                //  whole += triesInt.get(k).getTrans();
                // System.out.println(tr.count);
                //triesInt.put(k, tr);

                //  nodes1 += triesInt.get(k).getCount();
            }
            // System.out.println(nodes1);
            // System.out.println(whole);
        } catch (IOException ex) {
            Logger.getLogger(TopDown.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void recursiveIntersectionTrieNoTransNoPruning(ArrayList<String> order, int level, String fName, String nod, int scoreAll, String newNode, String oldNode, Map<ArrayList<String>, Integer> ups, TrieInt tr, Set<TrieNode> trieNodes, int maxB, boolean print) throws IOException, CloneNotSupportedException {
        int max = maxB;
        if (level == 1) {
            long startTime = System.currentTimeMillis();

            //this.setSubse(fName, "");
            this.setTriesIntNoPruning(fName, max);

            Map<String, Map<String, Integer>> subsetsB = this.getSubse();

            for (int j = 1; j <= max; j++) {
                int score = 0;
                String node = Integer.toString(j);
                Map<ArrayList<String>, Integer> ups1 = new HashMap();
                //System.out.println(node + " " + score);
                // TrieInt tr1 = triesInt.get(node);
                TrieInt tr1 = triesInt.get("1");
                //  for (int i = Integer.parseInt(node) + 1; i <= max; i++) {

                String newN = node;//+ "," + i;

                recursiveIntersectionTrieNoTransNoPruning(order, 2, null, newN, score, Integer.toString(j), node, ups1, tr1, null, maxB, print);//tr1.getTr().get(node));
                //}
            }
        } else {

            nodes++;
            level++;
            if (nodes % 10000 == 0) {
                //  System.out.println(nodes);
            }
            int score = 0;// scoreAll;
            tr.countRecursion=0;
            tr.setWholeScore(0);
            tr.nextNodes.clear();
            if (trieNodes == null) {
                tr.searchAndNext(null, Integer.parseInt(newNode));
            } else {
                
                for (TrieNode tri : trieNodes) {
                    tr.searchAndNext(tri, Integer.parseInt(newNode));
                }
            }
            HashSet<TrieNode> triej = new HashSet<TrieNode>(tr.getNextNodes());
            checks += tr.countRecursion;
            score += tr.getWholeScore();
            if (print == true) {
                System.out.println(nod + " " + score);
            }
            //     if(level==4 || score==0)
            //       return;
            //    }
            //}
            // int cScore = tr.wholeScore;
            // score += cScore;
            // HashSet<TrieNode> traverse = new HashSet<>(trieN);
//tr.getTrieNodes().clear();
            for (int i = Integer.parseInt(newNode) + 1; i <= max; i++) {

                String node = nod + "," + i;
                recursiveIntersectionTrieNoTransNoPruning(order, level, null, node, score, Integer.toString(i), nod, null, tr, triej, maxB, print);

            }

        }
    }

    public void recursiveSFUnionNoPruning(ArrayList<String> order, int level, String fName, String nod, int scoreAll, String newNode, String oldNode, Map<ArrayList<String>, Integer> ups, int maxB, boolean print) throws IOException, CloneNotSupportedException {
        int max = maxB;
        if (level == 2) {
            Map<ArrayList<String>, Integer> ups2 = this.setUnionDC(fName, 1, max);
            for (int j = 1; j <= max; j++) {
                String node = Integer.toString(j);
                int score = 0;
                Map<ArrayList<String>, Integer> ups1 = new HashMap<>(ups2);
                //System.out.println(j+" "+ups1.size());

                for (Iterator<Map.Entry<ArrayList<String>, Integer>> it = ups1.entrySet().iterator(); it.hasNext();) {
                    Map.Entry<ArrayList<String>, Integer> entry = it.next();
                    if (entry.getKey().contains(node)) {
                        int cScore = entry.getValue();
                        score += cScore;
                        it.remove();
                    }
                }
                Map<ArrayList<String>, Integer> upsN = new HashMap(ups1);
                //
                for (int i = j + 1; i <= max; i++) {
                    String newN = node + "," + i;
                    recursiveSFUnionNoPruning(order, 3, null, newN, score, Integer.toString(i), node, new HashMap(upsN), maxB, print);
                }
            }
        } else {

            nodes++;
            level++;
            if (nodes % 10000 == 0) {
                //  System.out.println(nodes);
            }
            int score = scoreAll;
            boolean gBreak = false;
            Set<ArrayList<String>> remove = new HashSet<>();

            for (Iterator<Map.Entry<ArrayList<String>, Integer>> it = ups.entrySet().iterator(); it.hasNext();) {
                Map.Entry<ArrayList<String>, Integer> entry = it.next();

                if (entry.getKey().contains(newNode)) {
                    int cScore = entry.getValue();
                    score += cScore;
                    it.remove();
                } else if (Integer.parseInt(entry.getKey().get(entry.getKey().size() - 1)) < Integer.parseInt(newNode)) {
                    // System.out.println(Integer.parseInt(entry.getKey().get(entry.getKey().size()-1))+" "+newNode);
                 //   it.remove();

                }

                // 
            }
            if (print == true) {
                System.out.println(nod + " " + score);
            }

            Map<ArrayList<String>, Integer> ups1 = new HashMap(ups);
            for (int i = Integer.parseInt(newNode) + 1; i <= max; i++) {
                String node = nod + "," + i;
                recursiveSFUnionNoPruning(order, level, null, node, score, Integer.toString(i), nod, new HashMap(ups), maxB, print);
            }

        }
    }

    public void recursiveUnionTrieNoPruning(ArrayList<String> order, int level, String fName, String nod, int scoreAll, String newNode, String oldNode, Set<String> nodes2, Set<TrieNode> tr, Trie2 trie2, int maxB, boolean print) throws IOException {
        int max = maxB;
        if (level == 1) {
            this.setTrieUnion(fName, max);
            for (int j = 1; j <= max; j++) {
                String node = Integer.toString(j);
                tries2.get("1").wholeScore = 0;
                //System.out.println(tries2.get("1").);
                tries2.get("1").searchAndNext(null, j);
                HashSet<TrieNode> triej = new HashSet<TrieNode>(tries2.get("1").getNextNodes());
                int score = tries2.get("1").getWholeScore();
                //  System.out.println(node + " " + score);
                for (int i = Integer.parseInt(node) + 1; i <= max; i++) {
                    Set<String> nodes3 = new TreeSet<String>();
                    nodes3.add(node);
                    // if(order.contains(i))
                    //   continue;
                    String newN = node + "," + i;// order.get(i);
                    recursiveUnionTrieNoPruning(order, 2, null, newN, score, Integer.toString(i), node, nodes3, triej, tries2.get("1"), maxB, print);
                }
            }

        } else {
            nodes++;
            if (nodes % 10000 == 0) {
                //    System.out.println(nodes);
            }
            level++;
            int score = scoreAll;
            trie2.setWholeScore(0);
            trie2.nextNodes.clear();
            checks = checks + tr.size();
            for (TrieNode tri : tr) {
                trie2.searchAndNext(tri, Integer.parseInt(newNode));
            }
            HashSet<TrieNode> triej = new HashSet<TrieNode>(trie2.getNextNodes());
            score += trie2.getWholeScore();
            //if(level==5)
            //   return;
            if (print == true) {
                System.out.println(nod + " " + score);
            }
            for (int i = Integer.parseInt(newNode) + 1; i <= max; i++) {
                nodes2.add(newNode);
                String node = nod + "," + i;//order.get(i);
                recursiveUnionTrieNoPruning(order, level, null, node, score, Integer.toString(i), nod, new HashSet(nodes2), triej, trie2, maxB, print);
            }

        }
    }

    public void DCS(ArrayList<String> order, int level, String fName, String nod, int scoreAll, String newNode, String oldNode, Map<ArrayList<String>, Integer> ups, int maxB, boolean print, HashMap<String, Integer> list) throws IOException, CloneNotSupportedException {
        int max = maxB;
        if (level == 2) {
            BufferedReader br = new BufferedReader(new FileReader(fName));
            String sCurrentLine = "";
            HashMap<String, Integer> dcList = new HashMap<>();
            while ((sCurrentLine = br.readLine()) != null) {
                String[] split = sCurrentLine.split("\t");
                // String[] split2=split[1].split(",");
                //if(dcList.containsKey(split[0]))
                //  System.out.println("hi");
                dcList.put(split[0], Integer.parseInt(split[1]));
            }
            Map<ArrayList<String>, Integer> ups2 = this.setUnionDC(fName, 1, max);
            for (int j = 1; j <= max; j++) {
                String node = Integer.toString(j);
                int score = 0;
                Map<ArrayList<String>, Integer> ups1 = new HashMap<>(ups2);
                for (Iterator<Map.Entry<ArrayList<String>, Integer>> it = ups1.entrySet().iterator(); it.hasNext();) {
                    Map.Entry<ArrayList<String>, Integer> entry = it.next();
                    if (entry.getKey().contains(node)) {
                        int cScore = entry.getValue();
                        score += cScore;
                        it.remove();
                    }
                }
                Map<ArrayList<String>, Integer> upsN = new HashMap(ups1);
                //
                for (int i = j + 1; i <= max; i++) {
                    String newN = node + "," + i;
                    DCS(order, 3, fName, newN, score, Integer.toString(i), node, new HashMap(upsN), maxB, print, dcList);
                }
            }
        } else {

            nodes++;
            level++;
            if (nodes % 10000 == 0) {
                //  System.out.println(nodes);
            }
            int score = 0;
            boolean gBreak = false;
            Set<ArrayList<String>> remove = new HashSet<>();

            BufferedReader br = new BufferedReader(new FileReader(fName));
            String sCurrentLine = "";
            // nod="5,6";
            for (String ls : list.keySet()) {
                HashSet<String> set = new HashSet<String>();
                for (String k : ls.split(",")) {
                    set.add(k);
                }
                boolean flag = true;
                for (String p : nod.split(",")) {
                    // System.out.println(nod+" "+p);
                    if (set.contains(p)) {
                        score += list.get(ls);
                        break;
                    }

                }

                //score+=list.get(ls);
            }
            if (print == true) {
                System.out.println(nod + " " + score);
            }

            Map<ArrayList<String>, Integer> ups1 = new HashMap(ups);
            for (int i = Integer.parseInt(newNode) + 1; i <= max; i++) {
                String node = nod + "," + i;
                DCS(order, level, fName, node, score, Integer.toString(i), nod, new HashMap(ups), maxB, print, list);
            }

        }
    }

    public void DCSIntersection(ArrayList<String> order, int level, String fName, String nod, int scoreAll, String newNode, String oldNode, Map<ArrayList<String>, Integer> ups, int maxB, boolean print, HashMap<String, Integer> list) throws IOException, CloneNotSupportedException {
        int max = maxB;
        if (level == 2) {
            BufferedReader br = new BufferedReader(new FileReader(fName));
            String sCurrentLine = "";
            HashMap<String, Integer> dcList = new HashMap<>();
            while ((sCurrentLine = br.readLine()) != null) {
                String[] split = sCurrentLine.split("\t");
                // String[] split2=split[1].split(",");
                //if(dcList.containsKey(split[0]))
                //  System.out.println("hi");
                dcList.put(split[0], Integer.parseInt(split[1]));
            }
            //Map<ArrayList<String>, Integer> ups2 = this.setUnionDC(fName, 1,max);
            for (int j = 1; j <= max; j++) {
                String node = Integer.toString(j);
                int score = 0;
                for (int i = j + 1; i <= max; i++) {
                    String newN = node + "," + i;
                    DCSIntersection(order, 3, fName, newN, score, Integer.toString(i), node, null, maxB, print, dcList);
                }
            }
        } else {

            nodes++;
            level++;
            if (nodes % 10000 == 0) {
                //  System.out.println(nodes);
            }
            int score = 0;
            boolean gBreak = false;
            Set<ArrayList<String>> remove = new HashSet<>();

            BufferedReader br = new BufferedReader(new FileReader(fName));
            String sCurrentLine = "";
            // nod="5,6";
            for (String ls : list.keySet()) {
                HashSet<String> set = new HashSet<String>();
                for (String k : ls.split(",")) {
                    set.add(k);
                }
                boolean flag = true;
                for (String p : nod.split(",")) {
                    // System.out.println(nod+" "+p);
                    if (!set.contains(p)) {
                        flag = false;
                        // score+=list.get(ls);
                        break;
                    }

                }
                if (flag == true) {
                    score += list.get(ls);
                }
                //score+=list.get(ls);
            }
            if (print == true) {
                System.out.println(nod + " " + score);
            }

            //Map<ArrayList<String>, Integer> ups1 = new HashMap(ups);
            for (int i = Integer.parseInt(newNode) + 1; i <= max; i++) {
                String node = nod + "," + i;
                DCSIntersection(order, level, fName, node, score, Integer.toString(i), nod, null, maxB, print, list);
            }

        }
    }

    public void TrieAdjSF(ArrayList<String> order, int level, String fName, String nod, int scoreAll, String newNode, String oldNode, Map<ArrayList<String>, Integer> ups, TrieInt tr, Set<TrieNode> trieNodes, int maxB, boolean print) throws IOException, CloneNotSupportedException {
        int max = maxB;
        if (level == 1) {
            long startTime = System.currentTimeMillis();

            //this.setSubse(fName, "");
            Map<String, Map<String, Integer>> subsetsB = this.getSubse();
            this.setTriesIntNoPruning(fName, max);
            for (int j = 1; j <= max; j++) {
                int score = 0;
                String node = Integer.toString(j);
                Map<ArrayList<String>, Integer> ups1 = new HashMap();
                //System.out.println(node + " " + score);
                //  System.out.println(triesInt.get("1").getCount());
                // triesInt.clear();

                //
                TrieInt tr1 = triesInt.get("1");
                // System.out.println(tr1.getCount());
                for (int i = Integer.parseInt(node) + 1; i <= max; i++) {

                    String newN = node + "," + i;
                    TrieAdjSF(order, 2, null, newN, score, Integer.toString(i), node, ups1, tr1, tr1.root.getTransitive().get(node), maxB, print);
                }
                //System.out.println(nodes);
            }
        } else {
            nodes++;
            level++;
            if (nodes % 100000 == 0) {
                // System.out.println(nodes);
            }
            int score = 0;// scoreAll;
            int prevNodes = 0, checksNow = 0;
            Set<TrieNode> trieN = new HashSet<TrieNode>();
            // if(trieNodes!=null)
            // {
            boolean first = true;
            String[] dstets = nod.split(",");
            trieNodes = tr.root.getTransitive().get(dstets[0]);
            for (int i = 1; i < dstets.length; i++) {

                for (TrieNode tn : trieNodes) { //{ 

                    if (tn.getTransitive().containsKey(dstets[i])) {
                        for (TrieNode tn1 : tn.getTransitive().get(dstets[i])) {
                            // checksNow++;
                            if (i == (dstets.length - 1)) {
                                score += tn1.score;
                            }
                            if (!tn1.getTransitive().isEmpty()) {
                                trieN.add(tn1);
                            }
                            //tr.removeTrie(tn1, false, 0, Integer.parseInt(newNode));
                        }
                    }

                }
                
                // trieNodes.clear();
                if (trieN.size() == 0) {
                    break;
                }
                trieNodes = new HashSet<TrieNode>(trieN);
                // }
                trieN.clear();
            }

            if (print == true) {
                System.out.println(nod + " " + score);
            }
            // int cScore = tr.wholeScore;
            // score += cScore;
            // HashSet<TrieNode> traverse = new HashSet<>(trieN);
//tr.getTrieNodes().clear();
            for (int i = Integer.parseInt(newNode) + 1; i <= max; i++) {
                String node = nod + "," + i;
                TrieAdjSF(order, level, null, node, score, Integer.toString(i), nod, null, tr, null, maxB, print);

            }

        }
    }

    public void TrieSF(ArrayList<String> order, int level, String fName, String nod, int scoreAll, String newNode, String oldNode, Map<ArrayList<String>, Integer> ups, TrieInt tr, Set<TrieNode> trieNodes, int maxB, boolean print) throws IOException, CloneNotSupportedException {
        int max = maxB;
        if (level == 1) {
            long startTime = System.currentTimeMillis();

            //this.setSubse(fName, "");
            Map<String, Map<String, Integer>> subsetsB = this.getSubse();
            this.setTriesIntNoPruning(fName, max);
            for (int j = 1; j <= max; j++) {
                int score = 0;
                String node = Integer.toString(j);
                Map<ArrayList<String>, Integer> ups1 = new HashMap();
                //System.out.println(node + " " + score);
                //  System.out.println(triesInt.get("1").getCount());
                // triesInt.clear();

                //TrieSF
                TrieInt tr1 = triesInt.get("1");
                // System.out.println(tr1.getCount());
                for (int i = Integer.parseInt(node) + 1; i <= max; i++) {

                    String newN = node + "," + i;
                    //System.out.println(tr1.getTr().get(node).size());
                    // if (!tr1.getTr().containsKey(node)) {
                    // System.out.println("hi");
                    //  continue;
                    // }

                    TrieSF(order, 2, null, newN, score, Integer.toString(i), node, ups1, tr1, tr1.root.getTransitive().get(node), maxB, print);
                }
                //System.out.println(nodes);
            }
        } else {
            nodes++;
            level++;
            if (nodes % 100000 == 0) {
                // System.out.println(nodes);
            }
            int score = 0;// scoreAll;
            int prevNodes = 0, checksNow = 0;
            Set<TrieNode> trieN = new HashSet<TrieNode>();
            // if(trieNodes!=null)
            // {
            tr.getNextNodes().clear();

            boolean first = true;
            String[] dstets = nod.split(",");
            for (int i = 0; i < dstets.length; i++) {

                if (first == true) {
                    tr.getNextNodes().clear();
                    tr.searchAndNext(null, Integer.parseInt(dstets[i]));
                    first = false;
                } else {
                    tr.getNextNodes().clear();
                    checks += trieNodes.size();
                    tr.setWholeScore(0);
                    for (TrieNode tri : trieNodes) {
                        tr.searchAndNext(tri, Integer.parseInt(dstets[i]));
                    }

                }
                //trieNodes.clear();
                trieNodes = new HashSet<TrieNode>(tr.getNextNodes());
                if (dstets.length - 1 == i) {
                    score = tr.getWholeScore();
                }

                // trieNodes.clear();
                //     trieNodes=new HashSet<TrieNode>(trieN);
                // }
            }

            if (print == true) {
                System.out.println(nod + " " + score);
            }
            // int cScore = tr.wholeScore;
            // score += cScore;
            // HashSet<TrieNode> traverse = new HashSet<>(trieN);
//tr.getTrieNodes().clear();
            for (int i = Integer.parseInt(newNode) + 1; i <= max; i++) {
                String node = nod + "," + i;
               TrieSF(order, level, null, node, score, Integer.toString(i), nod, null, tr, trieN, maxB, print);

            }

        }
    }

    public void SFUnionTrie(ArrayList<String> order, int level, String fName, String nod, int scoreAll, String newNode, String oldNode, Set<String> nodes2, Set<TrieNode> tr, Trie2 trie2, int maxB, boolean print) throws IOException {
        int max = maxB;
        if (level == 1) {
            this.setTrieUnion(fName, max);
            for (int j = 1; j <= max; j++) {
                String node = Integer.toString(j);
                tries2.get("1").wholeScore = 0;
                //System.out.println(tries2.get("1").);
                tries2.get("1").searchAndNext(null, j);
                HashSet<TrieNode> triej = new HashSet<TrieNode>(tries2.get("1").getNextNodes());
                int score = tries2.get("1").getWholeScore();
                //  System.out.println(node + " " + score);
                for (int i = Integer.parseInt(node) + 1; i <= max; i++) {
                    Set<String> nodes3 = new TreeSet<String>();
                    nodes3.add(node);
                    // if(order.contains(i))
                    //   continue;
                    String newN = node + "," + i;// order.get(i);
                    SFUnionTrie(order, 2, null, newN, score, Integer.toString(i), node, nodes3, triej, tries2.get("1"), maxB, print);
                }
            }

        } else {
            nodes++;
            if (nodes % 10000 == 0) {
                //    System.out.println(nodes);
            }
            level++;
            //int score = scoreAll;
            trie2.setWholeScore(0);

            checks = checks + tr.size();
            String[] dstets = nod.split(",");
            trie2.searchAndNext(null, Integer.parseInt(dstets[0]));
            int score = 0;// trie2.getWholeScore();

            HashSet<TrieNode> triej = new HashSet<TrieNode>(trie2.getNextNodes());
            trie2.nextNodes.clear();
            for (int i = 1; i < dstets.length; i++) {

                for (TrieNode tri : triej) {
                    trie2.searchAndNext(tri, Integer.parseInt(dstets[i]));
                }
                triej = new HashSet<TrieNode>(trie2.getNextNodes());
                trie2.nextNodes.clear();

            }
            score += trie2.getWholeScore();
            //if(level==5)
            //   return;
            if (print == true) {
                System.out.println(nod + " " + score);
            }
            for (int i = Integer.parseInt(newNode) + 1; i <= max; i++) {
                nodes2.add(newNode);
                String node = nod + "," + i;//order.get(i);
                SFUnionTrie(order, level, null, node, score, Integer.toString(i), nod, new HashSet(nodes2), triej, trie2, maxB, print);
            }

        }
    }

    public void recursiveSFIntersectionNoPruning(ArrayList<String> order, int level, String fName, String nod, int scoreAll, String newNode, String oldNode, Map<ArrayList<String>, Integer> ups, TrieInt tr, int maxB, boolean print) throws IOException, CloneNotSupportedException {
        int max = maxB;
        if (level == 2) {
            Map<ArrayList<String>, Integer> ups2 = this.setUnionDC(fName, 1, max);

            for (int j = 1; j <= max; j++) {
                String node = Integer.toString(j);
                int score = 0;
                //System.out.println(node + " " + ups1.size());
                Map<ArrayList<String>, Integer> ups1 = new HashMap<>(ups2);
                for (Iterator<Map.Entry<ArrayList<String>, Integer>> it = ups1.entrySet().iterator(); it.hasNext();) {
                    checks++;
                    Map.Entry<ArrayList<String>, Integer> entry = it.next();
                    if (!entry.getKey().contains(node)) {
                        int cScore = entry.getValue();
                        it.remove();
                    } else {
                        int cScore = entry.getValue();
                        score += cScore;

                    }
                }
                //  Map<ArrayList<String>, Integer> upsN = new HashMap(ups1);

                //System.out.println(node + " " + score);
                for (int i = j + 1; i <= max; i++) {
                    TrieInt tr1 = triesInt.get(Integer.toString(i - 1));
                    String newN = node + "," + i;
                    recursiveSFIntersection(order, 3, null, newN, score, Integer.toString(i), node, new HashMap(ups1), tr1, maxB, print);
                }
            }
        } else {
            nodes++;

            if (nodes % 10000 == 0) {
                //System.out.println(nodes);
            }
            int score = 0;// scoreAll;
            boolean gBreak = false;
            Set<ArrayList<String>> remove = new HashSet<>();

            for (Iterator<Map.Entry<ArrayList<String>, Integer>> it = ups.entrySet().iterator(); it.hasNext();) {
                Map.Entry<ArrayList<String>, Integer> entry = it.next();
                checks++;
                if (!entry.getKey().contains(newNode)) {
                    int cScore = entry.getValue();
                    it.remove();
                } else {
                    int cScore = entry.getValue();
                    score += cScore;

                }

            }

//              for(Iterator<Map.Entry<ArrayList<String>,Integer>> it = ups.entrySet().iterator(); it.hasNext(); ) {
//                Map.Entry<ArrayList<String>,Integer> entry = it.next();
//              int cScore = entry.getValue();
//                         score += cScore;
//                
//              }
//            Map<ArrayList<String>, Integer> ups1 = new HashMap(ups);
            //if(tr.containsKey(
            if (print == true) {
                System.out.println(nod + " " + score);
            }
            //}
            //if(score<5)
            ///  return;
            //if (level == 4  || score == 0) {
            //   if(score>0)
            // System.out.println(nod + " " + score);
            //   return;
            //}

            level++;
            for (int i = Integer.parseInt(newNode) + 1; i <= max; i++) {
                String node = nod + "," + i;
                recursiveSFIntersection(order, level, null, node, score, Integer.toString(i), nod, new HashMap(ups), tr, maxB, print);

            }

        }
    }

    public HashMap<ArrayList<String>, Integer> setIntersectionDCNoPruning(String fname, int i, int max) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(fname));
            String s;

            HashMap<ArrayList<String>, Integer> ret = new HashMap<>();
            int sc = 0;
            HashMap<Integer, Integer> sum = new HashMap<>();
            int count = 0;
            while ((s = br.readLine()) != null) {
                // if(s.contains("6"))
                ArrayList<String> dtsets = new ArrayList<String>();
                boolean flag = false;
                for (String k : s.split("\t")[0].split(",")) {
                    if (Integer.parseInt(k) <= max) {
                        dtsets.add(k);
                    }
                    if (Integer.parseInt(k) == i) {
                        flag = true;
                    }
                    if (Integer.parseInt(k) > i && flag == false) {
                        break;
                    }
                }
                if (flag == false) {
                    continue;
                }
                if (dtsets.size() > 0 && Integer.parseInt(dtsets.get(dtsets.size() - 1)) >= i) {
                    if (ret.containsKey(dtsets)) {
                        ret.put(dtsets, ret.get(dtsets) + Integer.parseInt(s.split("\t")[1]));
                    } else {
                        ret.put(dtsets, Integer.parseInt(s.split("\t")[1]));
                    }
                }
                // dtsets.addAll(Arrays.asList(s.split("\t")[0].split(",")));
                // int last = Integer.parseInt(s.split("\t")[0].split(",")[s.split("\t")[0].split(",").length - 1]);
                //System.out.println(last+" "+i);
                //if (last >= i) {
                //    ret.put(dtsets, Integer.parseInt(s.split("\t")[1]));
                // }

            }
            count += ret.size();
            // System.out.println(count);
            return ret;

        } catch (FileNotFoundException ex) {
            Logger.getLogger(TopDown.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TopDown.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                br.close();
            } catch (IOException ex) {
                Logger.getLogger(TopDown.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    
    
    public void recursiveUnionTrieWebApp( int level, String fName, String nod, int scoreAll, String newNode, String oldNode, Set<String> nodes2, Set<TrieNode> tr, Trie2 trie2, int maxB, boolean print, int desiredK, int topK) throws IOException {
        int max = maxB;
        if (level == 1) {
            this.setTrieUnionWebApp(fName, max);
            for (int j = 1; j <= max; j++) {
                String node = Integer.toString(j);
                //tries2.get("1").wholeScore=0;
                //System.out.println(tries2.get("1").);
                tries2.get(node).searchAndNext(null, j);
                HashSet<TrieNode> triej = new HashSet<TrieNode>(tries2.get(node).getNextNodes());
                int score = tries2.get(node).getWholeScore();
                //  System.out.println(node + " " + score);
                //if (print == true) {
                // System.out.println(node + " " + score);
                // }
                for (int i = Integer.parseInt(node) + 1; i <= max; i++) {
                    Set<String> nodes3 = new TreeSet<String>();
                    nodes3.add(node);
                    // if(order.contains(i))
                    //   continue;
                    String newN = node + "," + i;// order.get(i);
                    recursiveUnionTrieWebApp( 2, null, newN, score, Integer.toString(i), node, nodes3, triej, tries2.get(node), maxB, print,desiredK,topK);
                }
            }

        } else {
            nodes++;
          
            trie2.countRecursion = 0;
            int score = scoreAll;
            trie2.setWholeScore(0);
            trie2.nextNodes.clear();

            for (TrieNode tri : tr) {
                trie2.searchAndNext(tri, Integer.parseInt(newNode));
            }
            checks = checks + trie2.countRecursion;
            HashSet<TrieNode> triej = new HashSet<TrieNode>(trie2.getNextNodes());
            score += trie2.getWholeScore();
            if(desiredK==level){
                if(!ranking.isEmpty()&&ranking.firstKey()>score && ranking.size()>=topK)
                    return;
                if(ranking.containsKey(score)){
                    ranking.get(score).add(nod);
                }
                else{
                    HashSet<String> node=new HashSet<String>();
                    node.add(nod);
                    ranking.put(score, node);
                }
                if(ranking.size()>topK)
                    ranking.remove(ranking.firstKey());
               //  System.out.println(nod + " " + score);
                return;
            }
              level++;
            for (int i = Integer.parseInt(newNode) + 1; i <= max; i++) {
                nodes2.add(newNode);
                String node = nod + "," + i;//order.get(i);
                recursiveUnionTrieWebApp(level, null, node, score, Integer.toString(i), nod, new HashSet(nodes2), triej, trie2, maxB, print,desiredK,topK);
            }

        }
    }
        
        
        public void setTrieUnionWebApp(String fname, int max) {

            for (int i = 0; i <= max; i++) {
                Trie2 tr = new Trie2();
                String[] split=fname.split("\n");
                for(String s:split) {
                    if (s.split("\t").length == 2) {
                        tr.insert(s.split("\t")[0], Integer.parseInt(s.split("\t")[1]), i, max);
                    }

                }
                tries2.put(Integer.toString(i), tr);
            
            }
          
        }
        
    }
    
    
    


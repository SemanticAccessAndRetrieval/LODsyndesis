/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.forth.ics.isl.lattice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author micha
 */
public class LatticeMeasurements {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, CloneNotSupportedException {
        TopDown td = new TopDown();
        ArrayList<String> order = new ArrayList<String>();
        String ordered = "0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30";//"4,5,1,2,0,3";
        for (String x : ordered.split(",")) {
            order.add(x);
        }

        Scanner keyboard = new Scanner(System.in);
        System.out.println("Select the Measurement Type: Commonalities or Coverage");
        String type = keyboard.next();

        //"datasets/"+"triplesFull.txt";
      
        
        if (type.equals("Commonalities")) {

            System.out.println("Select the Dataset: Entities, Literals, Triples");
            String dataset = keyboard.next();
            String intDataset = "datasets/commonalities/" + dataset + ".txt";
            
            System.out.println("Select the Desired Approach for computing Commonalities:"+
                    "DCS,TR,TRFWD,LBDC,LBDC_NOPR,LBTRFWD,LBTRFWD_NOPR,LBTR,LBTR_NOPR");
            String choice = keyboard.next();
            
            
            System.out.println("Select the minimum Number of Subsets:");
            int min = keyboard.nextInt();
             System.out.println("Select the maximum Number of Subsets:");
             int max= keyboard.nextInt();
            System.out.println("Print the commonalities of all the subsets? true or false");
             boolean print2= keyboard.nextBoolean();
            String[] choices = {"", ""};
            //for (String choice : choices) {
                for (int i = min; i <= max; i++) {
                    long startTime = System.currentTimeMillis();
                    td.checks = 0;
                    //SF DirectCount
                    // 
                    if (choice.equals("DCS")) {
                        td.DCSIntersection(order, 2, intDataset, null, 0, "0", "", null, i, print2, null);
                    } else if (choice.equals("TRFWD")) {
                        //SF TRIE
                        td.TrieAdjSF(order, 1, intDataset, null, 0, "0", "", null, null, null, i, print2);
                        //TO ADD
                        //  td.TrieSF(order,1, intDataset, null,0,"0","",null,null,null,i,print2);
                    } else if (choice.equals("TR")) {
                        td.TrieSF(order, 1, intDataset, null, 0, "0", "", null, null, null, i, print2);
                    } else if (choice.equals("LBDC")) {
                        td.recursiveSFIntersection(order, 2, intDataset, null, 0, "0", "", null, null, i, print2);
                    } else if (choice.equals("LBDC_NOPR")) {
                        td.recursiveSFIntersectionNoPruning(order, 2, intDataset, null, 0, "0", "", null, null, i, print2);
                    } else if (choice.equals("LBTRFWD")) {
                        td.recursiveIntersectionTrie(order, 1, intDataset, null, 0, "0", "", null, null, null, i, print2);
                    } else if (choice.equals("LBTRFWD_NOPR")) {
                        td.recursiveIntersectionTrieNoPruning(order, 1, intDataset, null, 0, "0", "", null, null, null, i, print2);
                    } else if (choice.equals("LBTR")) {
                        td.recursiveIntersectionTrieNoTrans(order, 1, intDataset, null, 0, "0", "", null, null, null, i, print2);
                    } else if (choice.equals("LBTR_NOPR")) {
                        td.recursiveIntersectionTrieNoTransNoPruning(order, 1, intDataset, null, 0, "0", "", null, null, null, i, print2);
                    }
                    else{
                        continue;
                    }
                    long estimatedTime = System.currentTimeMillis() - startTime;
                    if (!choice.equals("")) {
                        System.out.println(+i + " Subsets\tApproach:" + choice + "\tTime (Seconds):" + (double) estimatedTime / (1000));
                    }

            //    }
            }
        } else if (type.equals("Coverage")) {
//Union Case        
        System.out.println("Select the Dataset: EntitiesDesc, LiteralsDesc, TriplesDesc, EntitiesSemiDesc,EntitiesMedium,EntitiesSemiAsc,EntitiesAsc");
            String dataset = keyboard.next();
            String unionDataset = "datasets/coverage/" + dataset + ".txt";
            
            System.out.println("Select the Desired Approach for computing Coverage:"+
                    "DCS,LB,LB+PR");//@TR,LBTR,LBTR_NOPR");
            String choice2 = keyboard.next();

            System.out.println("Select the minimum Number of Subsets:");
            int min = keyboard.nextInt();
             System.out.println("Select the maximum Number of Subsets:");
             int max= keyboard.nextInt();
             System.out.println("Print the coverage of all the subsets? true or false");
             boolean print= keyboard.nextBoolean();
            
            String[] choices2 = {""};
           // String unionDataset = "datasets/entitiesDesc.txt";
            //for (String unionDataset : datasets) {
          
                for (int i = min; i <= max; i++) {
                    long startTime = System.currentTimeMillis();
                    //  td.setTrieUnion(unionDataset,i);
                    td.checks = 0;
                    if (choice2.equals("DCS")) {
                        //SF DirectCount
                        td.DCS(order, 2, unionDataset, null, 0, "0", "", null, i, print, null);
                    } else if (choice2.equals("TR")) {
                        td.SFUnionTrie(order, 1, unionDataset, null, 0, "0", "", null, null, null, i, print);

                    } else if (choice2.equals("LB+PR")) {
                        td.recursiveSFUnion(order, 2, unionDataset, null, 0, "0", "", null, i, print);
                        

                    } else if (choice2.equals("LBTR")) {
                        td.recursiveUnionTrie(order, 1, unionDataset, null, 0, "0", "", null, null, null, i, print);
                    } else if (choice2.equals("LB")) {
                        td.recursiveSFUnionNoPruning(order, 2, unionDataset, null, 0, "0", "", null, i, print);

                    } else if (choice2.equals("LBTR_NOPR")) {
                        td.recursiveUnionTrieNoPruning(order, 1, unionDataset, null, 0, "0", "", null, null, null, i, print);
                    }
                    else{
                        continue;
                    }

                    long estimatedTime = System.currentTimeMillis() - startTime;
                    if (!choice2.equals("")) {
                       System.out.println(+i + " Subsets\tApproach:" + choice2 + "\tTime (Seconds):\t" + (double) estimatedTime / (1000)+"\t"+(double) td.checks/(Math.pow(2, i)));
                    
                    }

                
                //  }
            }
        }
    }
    
    
    

}

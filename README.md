# LODsyndesis: Connectivity of Linked Open Datasets
<html>
<body>
This page contains the code for creating the indexes and measurements of LODsyndesis (see  <a href="www.ics.forth.gr/isl/LODsyndesis/">LODsyndesis website</a> for more information). By executing the LODsyndesis.jar, one can create <ul>
<li>the Prefix Index and SameAsPrefixIndex, </li>
<li>the SameAsCatalog, </li>
<li>the Element Index, </li>
<li>the Common Literals Index, </li>
<li>the Lattice of Common Elements among any subset of sources.</li>
</ul> 

<h2> Datasets</h2>
The datasets for creating the LODsyndesis indexes can be found in <a href="https://datahub.io/dataset/connectivity-of-lod-datasets">datahub </a>, where one can download all the URIs and the sameAs relationships of 302 LOD Datasets (<a href="https://old.datahub.io/dataset/connectivity-of-lod-datasets/resource/8baffae2-46ab-4639-b2d0-d836f12df873">URIs of datasets</a>) and all the Literals of 302 Datasets (<a href="https://old.datahub.io/dataset/connectivity-of-lod-datasets/resource/7a98fad6-e101-4530-a578-065fd8138468">Literals of datasets</a>). 

<h2>How to Create the Indexes</h2>
First, one should upload the datasets in a specific folder (e.g., in HDFS). Below, we describe the commands that one should use for create the indexes and a specific example.

<h3> Create the Prefix Indexes</h3>
<b> Command for creating the Prefix and the SameAsPrefixIndex:</b>
hadoop jar LODsyndesis.jar gr.forth.ics.isl.indexes.CreatePrefixIndex &lt;Datasets Folder&gt; &lt;Output Folder&gt; &lt;Number of Reducers&gt;  <br>
where <br>
&lt;Datasets folder&gt;: The folder containing the URIs of the datasets. <br>
&lt;Output folder&gt;: The output folder for storing the prefix indexes. <br>
&lt;Number of Reducers&gt;: The number of reducers to be used. <br>
  

<h3> Create the SameAs Neighbors</h3>
<b> Command for creating the SameAsNeighbors: </b>
hadoop jar LODsyndesis.jar gr.forth.ics.isl.sameAsCatalog.GetNeighborsSameAs &lt;SameAs relationships Path&gt; &lt;SameAs Neighbors Folder&gt; &lt;Number of Reducers&gt; <br>
where <br>
&lt;SameAs Relationships Path&gt;: The path containing the sameAs relationships <br>
&lt;SameAs Neighbors folder&gt;: The output folder containing the sameAs Neighbors <br>
&lt;Number of Reducers&gt;: The number of reducers to be used. <br>


<h3> Create the SameAs Catalog</h3>
<b> Command for running the SameAs HashToMin algorithm: </b>
hadoop jar LODsyndesis.jar gr.forth.ics.isl.sameAsCatalog.HashToMin &lt;SameAs Neighbors Folder&gt; &lt;Output Folder&gt; &lt;SameAsPrefix Index Path&gt; &lt;Number of Reducers&gt;  &lt;Threshold for Using Signature Algorithm&gt; &lt;Value for Enabling SameAsPrefixIndex&gt; <br>
where <br>
&lt;SameAs Neighbors folder&gt;: The folder containing the sameAs Neighbors <br>
&lt;Output folder&gt;: The output folder for storing the sameAsCatalog. <br>
&lt;Number of Reducers&gt;: The number of reducers to be used. <br>
&lt;SameAsPrefix Index Path&gt; : The path of the SameAsPrefix Index <br>  
&lt;Threshold for Using the Signature Algorithm&gt;: If the number of remaining URIs is less than a threshold, the signature algorithm will be used.<br>
&lt;Value for Enabling SameAsPrefixIndex&gt; Put 1 for using SameAsPrefixIndex or 0 for not using it.

<h3> Create the Element Index</h3>
<b> Command for running the Element Index: </b>
hadoop jar LODsyndesis.jar gr.forth.ics.isl.indexes.CreateElementIndex &lt;Input Folder&gt; &lt;Output Folder&gt; &lt;Prefix Index Path&gt; &lt;Number of Reducers&gt; <br>
where <br>
&lt;SameAs Neighbors folder&gt;: The folder containing the URIs and the sameAs Catalog <br>
&lt;Output folder&gt;: The output folder for storing the elementIndex. <br>
&lt;Prefix Index Path&gt; : The path of the Prefix Index <br>  
&lt;Number of Reducers&gt;: The number of reducers to be used. <br>
 
<h3> Create the Literals Index</h3>
<b> Command for running the Literals Index: </b>
hadoop jar LODsyndesis.jar gr.forth.ics.isl.indexes.CreateCommonLiteralsIndex &lt;Literals Folder&gt; &lt;Output Folder&gt;  &lt;Number of Reducers&gt; <br>
where <br>
&lt;Literals folder&gt;: The folder containing the literals<br>
&lt;Output folder&gt;: The output folder for storing the literals index. <br>
&lt;Number of Reducers&gt;: The number of reducers to be used. <br>


<h3> Create Direct Counts</h3>
<b> Command for creating DirectCounts: </b>
hadoop jar LODsyndesis.jar gr.forth.ics.isl.latticeCreation.CreateDirectCounts 
&lt;Index Folder&gt; &lt;Output Folder&gt; &lt;Number of Reducers&gt; <br>
where <br>
&lt;Index Folder &gt;: The folder containing an Index (e.g., literals index or element index) <br>
&lt;Output folder&gt;: The output folder for storing the direct counts. <br>
&lt;Number of Reducers&gt;: The number of reducers to be used. <br>
   

<h3> Create  Lattice</h3>
<b> Command for creating a lattice: </b>
hadoop jar LODsyndesis.jar gr.forth.ics.isl.latticeCreation.CreateLattice directCounts 
&lt;Direct Counts Folder&gt; &lt;Output Folder&gt; &lt;Number of Reducers&gt; &lt;Threshold of Common Elements&gt; &lt;Maximum Level to reach &gt; &lt;Save to File from Level X&gt; &lt;Save to File until Level Y&gt; &lt;Split Threshold&gt; <br>
where <br>
&lt;Direct Counts Folder &gt;: The folder containing the direct Counts  <br>
&lt;Output folder&gt;: The output folder for storing the measurements. <br>
&lt;Number of Reducers&gt;: The number of reducers to be used. <br>
&lt;Threshold t of Common Elements&gt;: Measure subsets having more than t common elements<br>
&lt;Maximum Level to reach&gt;: The maximum lattice level to reach<br>
&lt;Save to File from Level X&gt;: Save all the measurements starting from this lattive level X. <br>
&lt;Save to File until Level Y&gt;: Save all the measurements until this lattive level Y <br>
&lt;Split Threshold&gt;: A value [0,1] for configuring how to split the lattice in reducers <br>



<h3>Full Example for creating the indexes</h3>
For the examples below, we suppose that we have uploaded the URIs of the datasets in a folder called URIs/ and the literals in a folder called Literals/.

<b> Create Prefix Index by using one reducer: </b> hadoop jar LODsyndesis.jar gr.forth.ics.isl.indexes.CreatePrefixIndex URIs prefixIndexes 1<br>
Output: Prefix Index file--> prefixIndexes/prefixIndex/prefixIndex.txt-r-00000 <br>
SameAsPrefix Index file--> sameAsPrefix/sameAsPrefix.txt-r-00000 <br>

<b> Create SameAs Neighbors by using 32 reducers: </b>hadoop jar LODsyndesis.jar gr.forth.ics.isl.sameAsCatalog.GetNeighborsSameAs URIs/sameAs2_-1.txt nbrs 32  <br>
Output: SameAs neighbors folder--> nbrs/sameAsP <br>

<b>Create SameAs Catalog by using 32 Reducers:</b> hadoop jar LODsyndesis.jar gr.forth.ics.isl.sameAsCatalog.HashToMin nbrs/sameAsP sameAs prefixIndexes/sameAsPrefix/sameAsPrefix.txt-r-00000 32 1000000 1<br>
Output: It will perform 4 iterations and the SameAs Catalog can be found in 4 Parts--> sameAs/sameAs1/sameAsCatalog, sameAs/sameAs2/sameAsCatalog, sameAs/sameAs3/sameAsCatalog, sameAs/sameAs4/sameAsCatalog<br>

<b> Intermediate Steps</b>
Merge sameAsCatalog files and then upload it to the URIs folder <br>
  
hadoop fs -getmerge sameAs/sameAs1/sameAsCatalog/ sameAsCatalog1.txt <br>
hadoop fs -put sameAsCatalog1.txt URIS/  <br>
hadoop fs -getmerge sameAs/sameAs2/sameAsCatalog/ sameAsCatalog2.txt <br>
hadoop fs -put sameAsCatalog2.txt URIS/  <br>
hadoop fs -getmerge sameAs/sameAs3/sameAsCatalog/ sameAsCatalog3.txt <br>
hadoop fs -put sameAsCatalog3.txt URIS/  <br>
hadoop fs -getmerge sameAs/sameAs4/sameAsCatalog/ sameAsCatalog4.txt <br>
hadoop fs -put sameAsCatalog4.txt URIS/  <br>

Delete sameAs relationships from URIs/ folder. <br>
hadoop fs -rm URIs/sameAs2_-1.txt

<b>Create Element Index by using 32 Reducers:</b> hadoop jar LODsyndesis.jar gr.forth.ics.isl.indexes.CreateElementIndex URIs/ elementIndex prefixIndexes/prefixIndex/prefixIndex.txt-r-00000 32
<br>
Output: It will perform 2 iterations and the element Index can be found in 2 Parts--> elementIndex/Part1, elementIndex/Part2

<b> Intermediate Step</b>
Merge Element Index part 1 and part 2 <br>

hadoop fs -getmerge elementIndex/Part2/ part2.txt  <br>
hadoop fs -put part2.txt elementIndex/Part1/                 

<b>Create Common Literals Index by using 32 Reducers:</b> hadoop jar LODsyndesis.jar gr.forth.ics.isl.indexes.CreateCommonLiteralsIndex Liteals/ literalsIndex  32
<br>
Output: Common Literals Index--> literalsIndex

<b>Create Element Index Direct Counts by using 1 Reducer:</b>   hadoop jar LODsyndesis.jar gr.forth.ics.isl.latticeCreation.CreateDirectCounts elementIndex/Part1 directCounts 1
<br>
Output: Direct Counts of element Index--> directCounts

<b>Create Element Index Lattice by using 32 reducers:</b>   hadoop jar LODsyndesis.jar gr.forth.ics.isl.latticeCreation.CreateLattice directCounts lattice 32 1000 15 2 10 0.05 <br>

Description: It will measure the common elements between subsets of sources until level 15 having at least 1000 common elements. Moreover, it will save all the measurements from level 2 to level 10.
<br>
Output--> A folder lattice/Print containing the measurements for nodes from level 2 to 10 having at least 1000 common elements


<b>Create Common Literals Index Direct Counts by using 1 Reducer:</b>   hadoop jar LODsyndesis.jar gr.forth.ics.isl.latticeCreation.CreateDirectCounts  literalsIndex/ directCountsLiterals 1
<br> Output: Direct Counts of Literals --> directCountsLiterals <br>

<b>Create Literals Lattice by using 32 reducers:</b>   hadoop jar LODsyndesis.jar gr.forth.ics.isl.latticeCreation.CreateLattice directCountsLiterals latticeLiterals 32 1000 15 2 10 0.05 <br>

Description: It will measure the number of common literals between subsets of sources until level 15 having at least 1000 common literals. Moreover, it will save all the measurements from level 2 to level 10.
<br>
Output--> A folder latticeLiterals/Print containing the measurements for nodes from level 2 to 10 having at least 1000 common literals

</body>
  
  
</html>

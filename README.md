# LODsyndesis: Connectivity of Linked Open Datasets
<html>
<body>
This page contains the code for creating the indexes and measurements of LODsyndesis (see  <a href="www.ics.forth.gr/isl/LODsyndesis/">LODsyndesis website</a> for more information). By executing the LODsyndesis.jar, one can create <ul>
<li>the Prefix Index and SameAsPrefixIndex, </li>
<li>the SameAsCatalog, </li>
<li>the Real World Triples </li>
<li>the Entity Triples Index</li>
<li>the Entity Index, </li>
<li>the Property Index, </li>
<li>the Class Index, </li>
<li>the Literals Index, </li>
<li>the Lattice of Common Elements among any subset of sources.</li>
</ul> 

<h2> Datasets</h2>
The datasets for creating the LODsyndesis indexes can be found in <a href="http://islcatalog.ics.forth.gr/dataset/lodsyndesis">FORTH-ISL catalog</a>, 
where one can download all the triples, URIs and the sameAs relationships of 400 LOD Datasets. 

<h2>How to Create the Indexes</h2>
First, one should upload the datasets in a specific folder (e.g., in HDFS). Below, we describe the commands that one should use for create the indexes and a specific example.

<h3>Create Only the Entity Index <h3>

<h4> Create the Prefix Indexes</h4>
<b> Command for creating the Prefix and the SameAsPrefixIndex:</b>
hadoop jar LODsyndesis.jar gr.forth.ics.isl.indexes.CreatePrefixIndex &lt;Datasets Folder&gt; &lt;Output Folder&gt; &lt;Number of Reducers&gt;  <br>
where <br>
&lt;Datasets folder&gt;: The folder containing the URIs of the datasets. <br>
&lt;Output folder&gt;: The output folder for storing the prefix indexes. <br>
&lt;Number of Reducers&gt;: The number of reducers to be used. <br>
  

<h4> Create the SameAs Neighbors</h4>
<b> Command for creating the SameAsNeighbors: </b>
hadoop jar LODsyndesis.jar gr.forth.ics.isl.sameAsCatalog.GetNeighborsSameAs &lt;SameAs relationships Path&gt; &lt;SameAs Neighbors Folder&gt; &lt;Number of Reducers&gt; <br>
where <br>
&lt;SameAs Relationships Path&gt;: The path containing the sameAs relationships <br>
&lt;SameAs Neighbors folder&gt;: The output folder containing the sameAs Neighbors <br>
&lt;Number of Reducers&gt;: The number of reducers to be used. <br>


<h4> Create the SameAs Catalog</h4>
<b> Command for running the SameAs HashToMin algorithm: </b>
hadoop jar LODsyndesis.jar gr.forth.ics.isl.sameAsCatalog.HashToMin &lt;SameAs Neighbors Folder&gt; &lt;Output Folder&gt; &lt;SameAsPrefix Index Path&gt; &lt;Number of Reducers&gt;  &lt;Threshold for Using Signature Algorithm&gt; &lt;Value for Enabling SameAsPrefixIndex&gt; <br>
where <br>
&lt;SameAs Neighbors folder&gt;: The folder containing the sameAs Neighbors <br>
&lt;Output folder&gt;: The output folder for storing the sameAsCatalog. <br>
&lt;Number of Reducers&gt;: The number of reducers to be used. <br>
&lt;SameAsPrefix Index Path&gt; : The path of the SameAsPrefix Index <br>  
&lt;Threshold for Using the Signature Algorithm&gt;: If the number of remaining URIs is less than a threshold, the signature algorithm will be used.<br>
&lt;Value for Enabling SameAsPrefixIndex&gt; Put 1 for using SameAsPrefixIndex or 0 for not using it.

<h3> Create the Entity (or Element) Index</h3>
<b> Command for running the Element Index: </b>
hadoop jar LODsyndesis.jar gr.forth.ics.isl.indexes.CreateElementIndex &lt;Input Folder&gt; &lt;Output Folder&gt; &lt;Prefix Index Path&gt; &lt;Number of Reducers&gt; <br>
where <br>
&lt;SameAs Neighbors folder&gt;: The folder containing the URIs and the sameAs Catalog <br>
&lt;Output folder&gt;: The output folder for storing the elementIndex. <br>
&lt;Prefix Index Path&gt; : The path of the Prefix Index <br>  
&lt;Number of Reducers&gt;: The number of reducers to be used. <br>



<h3>Create All the Indexes <h3>

<h4> Create the Real World Triples</h4>
<b> Command for running the real world triples algorithm: </b>
<b> First Job </b>
hadoop jar LODsyndesis.jar gr.forth.ics.isl.indexes.ReplaceSubjects 
&lt;Triples Folder&gt; &lt;Output Folder&gt; 
&lt;Number of Reducers&gt;  &lt;PropertyCatalog File&gt; &lt;ClassCatalog File&gt; <br>
where <br>
&lt;Triples Folder&gt;: The folder containing the Triples and the SameAsCatalog<br>
&lt;Output folder&gt;: The output folder for storing the real world triples <br>
&lt;Number of Reducers&gt;: The number of reducers to be used. <br>
&lt;PropertyCatalog File&gt; : The file containing the Property Equivalence Catalog<br>  
&lt;ClassCatalog File&gt;: The file containing the Classs Equivalence Catalog<br>

<br>
<b> Second Job </b>
hadoop jar LODsyndesis.jar gr.forth.ics.isl.indexes.ReplaceObjects 
&lt;Input Folder&gt; &lt;Output Folder&gt; 
&lt;Number of Reducers&gt;  <br>
where <br>
&lt;Input Folder&gt;: The folder containing the input (which is produced from the first job) and the SameAsCatalog<br>
&lt;Output folder&gt;: The output folder for storing the produced real world triples<br>
&lt;Number of Reducers&gt;: The number of reducers to be used. <br>


<h4> Create the Entity-Triples Index</h4>
<b> Command for running the entity-triples index algorithm: </b>
<b> First Job </b>
hadoop jar LODsyndesis.jar gr.forth.ics.isl.indexes.CreateEntityTriplesIndex 
&lt;Real World Triples Folder&gt; &lt;Output Folder&gt; 
&lt;Number of Reducers&gt;  &lt;Store All Triples? (Boolean Value)&gt; &lt;Store Some Triples  twice? (Boolean Value)&gt; <br>
where <br>
&lt;Real World Triples Folder&gt;: The folder containing the real world Triples<br>
&lt;Output folder&gt;: The output folder for storing the index <br>
&lt;Number of Reducers&gt;: The number of reducers to be used. <br>
&lt;Store All Triples?(Boolean Value)&gt;: Put 1 for storing Triples occuring in two or more datasets. Put 0 for storing all the triples.  <br>  
&lt;Store Some Triples  twice? (Boolean Value)&gt;: Put 1 for storing Triples once. Put 0 for storing triples having entities as objects twice<br>


<h4> Create Indexes for URIs</h4>
<b> Command for creating the Entity Index, Property Index and Class Index: </b>
hadoop jar LODsyndesis.jar gr.forth.ics.isl.indexes.CreateEntityIndex &lt;Real World Triples Folder&gt; &lt;Output Folder&gt; &lt;Prefix Index Path&gt; &lt;Number of Reducers&gt; <br>
where <br>
&lt;Real World Triples folder&gt;: The folder containing the real world triples <br>
&lt;Output folder&gt;: The output folder for storing the URI indexes. <br>
&lt;Number of Reducers&gt;: The number of reducers to be used. <br>





 
<h4> Create the Literals Index</h4>
<b> Command for running the Literals Index: </b>
hadoop jar LODsyndesis.jar gr.forth.ics.isl.indexes.CreateLiteralsIndex &lt;Real World Triples Folder&gt; &lt;Output Folder&gt;  &lt;Number of Reducers&gt; <br>
where <br>
&lt;Real World Triples folder&gt;: The folder containing the Real World Triples<br>
&lt;Output folder&gt;: The output folder for storing the literals index. <br>
&lt;Number of Reducers&gt;: The number of reducers to be used. <br>



<h3> Perform Lattice Measurements <h3>

<h4> Create Direct Counts for any index</h4>
<b> Command for creating DirectCounts for any index </b>
hadoop jar LODsyndesis.jar gr.forth.ics.isl.latticeCreation.CreateDirectCounts 
&lt;Index Folder&gt; &lt;Output Folder&gt; &lt;Number of Reducers&gt; <br>
where <br>
&lt;Index Folder &gt;: The folder containing an Index (e.g., literals index, properties index, etc.) <br>
&lt;Output folder&gt;: The output folder for storing the direct counts. <br>
&lt;Number of Reducers&gt;: The number of reducers to be used. <br>
   

<h4> Run Lattice Measurements</h4>
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



<h2>Full Example for creating the indexes</h2>

<h3>For constructing only Entity (or Element) Index </h3>
    Pre-Processing Steps: Download entities.zip and sameAs.zip from <a href="http://islcatalog.ics.forth.gr/dataset/lodsyndesis">FORTH-ISL catalog</a> and upload them to HDFS. <br>
		hadoop fs -mkdir URIs  <br>
		Unzip entities.zip and upload each file to HDFS: hadoop fs -put <file> URIs/  <br>
		Unzip sameAs.zip  <br>
		hadoop fs -mv 1000_sameAs.nt URIs/  <br>
		
	<b> Create Prefix Index by using one reducer: </b> hadoop jar LODsyndesis.jar gr.forth.ics.isl.indexes.CreatePrefixIndex URIs prefixIndexes 1<br><br>
	Output: Prefix Index file--> prefixIndexes/prefixIndex/prefixIndex.txt-r-00000 <br>
	SameAsPrefix Index file--> sameAsPrefix/sameAsPrefix.txt-r-00000 <br>

	<b> Create SameAs Neighbors by using 32 reducers: </b>hadoop jar LODsyndesis.jar gr.forth.ics.isl.sameAsCatalog.GetNeighborsSameAs URIs/1000_sameAs.nt nbrs 32  <br><br>
	Output: SameAs neighbors folder--> nbrs/sameAsP <br>

	<b>Create SameAs Catalog by using 32 Reducers:</b> hadoop jar LODsyndesis.jar gr.forth.ics.isl.sameAsCatalog.HashToMin nbrs/sameAsP sameAs prefixIndexes/sameAsPrefix/sameAsPrefix.txt-r-00000 32 1000000 1<br><br>
	Output: It will perform 4 iterations and the SameAs Catalog can be found in 4 Parts--> sameAs/sameAs1/sameAsCatalog, sameAs/sameAs2/sameAsCatalog, sameAs/sameAs3/sameAsCatalog, sameAs/sameAs4/sameAsCatalog<br>

	<b> Intermediate Steps:</b>
	Merge sameAsCatalog files and then upload them to the URIs folder <br>
	  
	hadoop fs -getmerge sameAs/sameAs1/sameAsCatalog/ sameAsCatalog1.txt <br>
	hadoop fs -put sameAsCatalog1.txt URIs/  <br>
	hadoop fs -getmerge sameAs/sameAs2/sameAsCatalog/ sameAsCatalog2.txt <br>
	hadoop fs -put sameAsCatalog2.txt URIs/  <br>
	hadoop fs -getmerge sameAs/sameAs3/sameAsCatalog/ sameAsCatalog3.txt <br>
	hadoop fs -put sameAsCatalog3.txt URIs/  <br>
	hadoop fs -getmerge sameAs/sameAs4/sameAsCatalog/ sameAsCatalog4.txt <br>
	hadoop fs -put sameAsCatalog4.txt URIs/  <br>

	
	<b>Create Entity Index by using 32 Reducers:</b> hadoop jar LODsyndesis.jar gr.forth.ics.isl.indexes.CreateElementIndex URIs/ elementIndex prefixIndexes/prefixIndex/prefixIndex.txt-r-00000 32
<br><br>
Output: It will perform 2 iterations and the element Index can be found in 2 Parts--> elementIndex/Part1, elementIndex/Part2

<b> Intermediate Step:</b>
Merge Element Index part 1 and part 2 <br>

hadoop fs -getmerge elementIndex/Part2/ part2.txt  <br>
hadoop fs -put part2.txt elementIndex/Part1/                 


<b>Create Entity Index Direct Counts by using 1 Reducer:</b>   hadoop jar LODsyndesis.jar gr.forth.ics.isl.latticeCreation.CreateDirectCounts elementIndex/Part1 directCounts 1
<br><br>
Output: Direct Counts of element Index--> directCounts

<b>Create Element Index Lattice by using 32 reducers:</b>   hadoop jar LODsyndesis.jar gr.forth.ics.isl.latticeCreation.CreateLattice directCounts lattice 32 100 15 2 5 0.05 <br><br>

Description: It will measure the common elements between subsets of sources until level 15 having at least 100 common elements.
Moreover, it will save all the measurements from level 2 to level 5.
<br><br>
Output: A folder lattice/Print containing the measurements for nodes from level 2 to 5 having at least 100 common elements


<h3>For constructing All the Indexes </h3>
	Pre-Processing Steps: Download catalogs.rar and all .rar. files starting with triples.part 
	from <a href="http://islcatalog.ics.forth.gr/dataset/lodsyndesis">FORTH-ISL catalog</a> and upload them to HDFS. <br>
		hadoop fs -mkdir Triples/ <br>
		Unrar all .rar files containing triples (6 different parts) and upload each file to HDFS hadoop fs -put <file> Triples/ <br>
		Unrar catalogs.rar <br>
		hadoop fs -put entityEquivalenceCatalog.txt Triples/  <br>
		hadoop fs -put propertyEquivalenceCatalog.txt  <br>
		hadoop fs -put classEquivalenceCatalog.txt <br>

<b>Create Real World Triples Index by using 32 Reducers:</b>
hadoop jar LODsyndesis.jar gr.forth.ics.isl.indexes.ReplaceSubjects Triples/ subjects  32 propertyEquivalenceCatalog.txt classEquivalenceCatalog.txt
<br><br>
Output: It will produce 2 subfolders --> subjects/finished, subjects/object <br>
The first folder contains the real world triples that have already been constructed, while the second folder contains the triples which need an additional job.  <br>
For running the second job, one should move entityEquivalenceCatalog.txt to subjects/object folder. The hadoop commands follow: <br>
hadoop fs -mv Triples/entityEquivalenceCatalog.txt subjects/object/ <br>

Then, one should run the following command: <br>
 
hadoop jar LODsyndesis.jar gr.forth.ics.isl.indexes.ReplaceObjects subjects/object objects/ 32 <br>
<br><br>
Output: It will produce 1 folder containing the second part of real world triples<br>
<br> 

<b> Intermediate Step:</b>
Collect All the real world triples in One Folder <br>
  
  
hadoop fs -mkdir realWorldTriples  
hadoop fs -mv subjects/finished/* realWorldTriples/ <br>
hadoop fs -mv objects/* realWorldTriples/ <br>

<b>Create Entity-Triples Index by using 32 Reducers:</b> hadoop jar LODsyndesis.jar gr.forth.ics.isl.indexes.CreateEntityTriplesIndex  realWorldTriples/ 
entityTriplesIndex 32 0 0
<br><br>
Output: It will produce a folder entityTriplesIndex containing the index

<b>Create Entity-Triples Index Direct Counts by using 1 Reducer:</b>  hadoop jar LODsyndesis.jar gr.forth.ics.isl.latticeCreation.CreateDirectCounts entityTriplesIndex/ dcTriples 1
<br><br>
Output: Direct Counts of Entity-Triples Index--> dcTriples

<b>Create Entity-Triples Index Lattice by using 32 reducers:</b>   
hadoop jar LODsyndesis.jar gr.forth.ics.isl.latticeCreation.CreateLattice dcTriples latticeTriples 32 100 15 2 5 0.05 <br><br>

Description: It will measure the common triples between subsets of sources until level 15 having at least 100 common triples.
Moreover, it will save all the measurements from level 2 to level 5.
<br><br>
Output: A folder latticeTriples/Print containing the measurements for nodes from level 2 to 5 having at least 100 common triples


<b>Create URI Indexes by using 32 Reducers:</b> hadoop jar LODsyndesis.jar gr.forth.ics.isl.indexes.CreateEntityIndex  realWorldTriples/ 
URI_Indexes 32 <br><br>
Output: It will produce a folder URI_Indexes, containing 3 subfolders: a)entities (i.e., Entity-Index) b) properties (i.e., Property-Index) and c) classes (i.e., Classes Index).


<b>Create Entity Index Direct Counts by using 1 Reducer:</b>  hadoop jar LODsyndesis.jar gr.forth.ics.isl.latticeCreation.CreateDirectCounts URI_Indexes/entities dcEntities 1
<br><br>
Output: Direct Counts of Entity Index--> dcEntities

<b>Create Entity Index Lattice by using 32 reducers:</b>   
hadoop jar LODsyndesis.jar gr.forth.ics.isl.latticeCreation.CreateLattice dcEntities latticeEntities 32 100 15 2 5 0.05  <br><br>

Description: It will measure the common entities between subsets of sources until level 15 having at least 100 common entities.
Moreover, it will save all the measurements from level 2 to level 5.
<br><br>
Output: A folder latticeEntities/Print containing the measurements for nodes from level 2 to 5 having at least 100 common entities.



<b>Create Property Index Direct Counts by using 1 Reducer:</b>  hadoop jar LODsyndesis.jar gr.forth.ics.isl.latticeCreation.CreateDirectCounts URI_Indexes/properties dcProperties 1
<br><br>
Output: Direct Counts of Property Index--> dcProperties

<b>Create Property Index Lattice by using 32 reducers:</b>   
hadoop jar LODsyndesis.jar gr.forth.ics.isl.latticeCreation.CreateLattice dcProperties latticeProperties 32 10 15 2 5 0.05  <br><br>

Description: It will measure the common properties between subsets of sources until level 15 having at least 10 common properties.
Moreover, it will save all the measurements from level 2 to level 5.
<br><br>
Output: A folder latticeProperties/Print containing the measurements for nodes from level 2 to 5 having at least 10 common properties.


<b>Create Class Index Direct Counts by using 1 Reducer:</b> hadoop jar LODsyndesis.jar gr.forth.ics.isl.latticeCreation.CreateDirectCounts URI_Indexes/classes dcClasses 1
<br><br>
Output: Direct Counts of Class Index--> dcClasses

<b>Create Class Index Lattice by using 32 reducers:</b>   
hadoop jar LODsyndesis.jar gr.forth.ics.isl.latticeCreation.CreateLattice dcClasses latticeClasses 32 10 15 2 5 0.05   <br><br>

Description: It will measure the common classes between subsets of sources until level 15 having at least 10 common classes.
Moreover, it will save all the measurements from level 2 to level 5.
<br><br>
Output: A folder latticeProperties/Print containing the measurements for nodes from level 2 to 5 having at least 10 common classes.



<b>Create Literals Index by using 32 Reducers:</b> hadoop jar LODsyndesis.jar gr.forth.ics.isl.indexes.CreateLiteralsIndex  realWorldTriples/ 
literalsIndex 32 <br><br>
Output: It will produce a folder literalsIndex.

<b>Create Literals Index Direct Counts by using 1 Reducer:</b>
 hadoop jar LODsyndesis.jar gr.forth.ics.isl.latticeCreation.CreateDirectCounts  literalsIndex/ dcLiterals 1<br><br>
Output: Direct Counts of Literals Index--> dcLiterals

<b>Create Literals Index Lattice by using 32 reducers:</b>   
hadoop jar LODsyndesis.jar gr.forth.ics.isl.latticeCreation.CreateLattice dcLiterals latticeLiterals 32 1000 10 2 5 0.05   <br><br>

Description: It will measure the common Literals between subsets of sources until level 8 having at least 1000 common Literals.
Moreover, it will save all the measurements from level 2 to level 5.
<br><br>
Output: A folder latticeProperties/Print containing the measurements for nodes from level 2 to 5 having at least 1000 common Literals.

</body>
  
  
</html>

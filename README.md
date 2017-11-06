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
First, one should upload the datasets in a specific folder (e.g., in HDFS). Below, we describe the commands that one should use for create the indexes and a specific example. For the examples below, we suppose that we have uploaded the URIs of the datasets in a folder called URIs/ and the literals in a folder called Literals/.#
<h3> Create the Prefix Indexes</h3>
<b> Command for creating the Prefix and the SameAsPrefixIndex</b> <br>
hadoop jar LODsyndesis.jar gr.forth.ics.isl.indexes.CreatePrefixIndex &lt;Datasets Folder&gt; &lt;Output Folder&gt; &lt;Number of Reducers&gt;  <br>
where <br>
&lt;Datasets folder&gt;: The folder containing the URIs of the datasets. <br>
&lt;Output folder&gt;: The output folder for storing the prefix indexes. <br>
&lt;Number of Reducers&gt;: The number of reducers to be used. <br>
  
<b>Example by using 1 Reducer:</b> hadoop jar LODsyndesis.jar gr.forth.ics.isl.indexes.CreatePrefixIndex URIs prefixIndexes 1<br>


<h3> Create the SameAs Catalog</h3>
<b> Command for running the SameAs HashToMin algorithm </b> <br>
hadoop jar LODsyndesis.jar gr.forth.ics.isl.sameAsCatalog.HashToMin &lt;SameAs Neigbors Folder&gt; &lt;Output Folder&gt; &lt;SameAsPrefix Index Path&gt; &lt;Number of Reducers&gt;  &lt;Threshold for Using Signature Algorithm&gt; &lt;Value for Enabling SameAsPrefixIndex&gt; <br>
where <br>
&lt;SameAs Neighbors folder&gt;: The folder containing the sameAs Neighbors <br>
&lt;Output folder&gt;: The output folder for storing the sameAsCatalog. <br>
&lt;Number of Reducers&gt;: The number of reducers to be used. <br>
&lt;<SameAsPrefix Index Path&gt; : The path of the SameAsPrefix Index <br>  
&lt;Threshold for Using the Signature Algorithm&gt; If the number of URIs is less than a threshold, the signature algorithm will be used <br>
&lt;Value for Enabling SameAsPrefixIndex&gt; Put 1 for using SameAsPrefixIndex or 0 for not using it.
  
  
  
<b>Example by using 32 Reducers:</b> hadoop jar LODsyndesis.jar gr.forth.ics.isl.sameAsCatalog.HashToMin nbrs/sameAsP prefixIndex/sameAsPrefix/sameAsPrefix.txt-r-00000 32 1000000 1<br>


  
</body>
  
  
</html>

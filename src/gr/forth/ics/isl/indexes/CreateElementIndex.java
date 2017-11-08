/*
This code belongs to the Semantic Access and Retrieval (SAR) group of the 
Information Systems Laboratory (ISL) of the 
Institute of Computer Science (ICS) of the  
Foundation for Research and Technology – Hellas (FORTH)

Nobody is allowed to use, copy, distribute, or modify this work.
It is published for reasons of research results reproducibility.

© 2017, Semantic Access and Retrieval group, All rights reserved

 */
package gr.forth.ics.isl.indexes;

import gr.forth.ics.isl.latticeCreation.CreateLattice;
import gr.forth.ics.isl.preliminary.Prefix;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;


public class CreateElementIndex extends Configured implements Tool {

	public static void main(String[] args) throws Exception {

		Configuration configuration = new Configuration();

		configuration.setBoolean("mapred.compress.map.output", true);

		int res = ToolRunner.run(configuration, new CreateElementIndex(),
				args);

		System.exit(res);
	}

	public static enum Counter {
		URISinONESOURCE, URISNUM, URISPOLICY1
	}

	@Override
	public int run(String[] args) throws Exception {
		Job job = new Job(getConf(), "ElementIndexPart1");
		job.setJarByClass(CreateElementIndex.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		job.setMapperClass(Map.class);
		job.setReducerClass(Reduce.class);

		job.setNumReduceTasks(Integer.parseInt(args[3]));

		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		job.addCacheFile(new Path(args[2]).toUri());
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		String[] m = { "sameAsSecond", "elementIndexPart1", "exactMatch" };
		for (String x : m) {
			MultipleOutputs.addNamedOutput(job, x, TextOutputFormat.class,
					Text.class, Text.class);

		}
		job.waitForCompletion(true);
		Job job2 = new Job(getConf(), "elementIndexPart2");
		job2.setJarByClass(CreateLattice.class);
		job2.setOutputKeyClass(Text.class);
		job2.setOutputValueClass(Text.class);
		job2.setMapperClass(Map2.class);
		job2.setReducerClass(Reduce2.class);
        job2.setNumReduceTasks(Integer.parseInt(args[3]));
        job2.setInputFormatClass(TextInputFormat.class);
        job2.setOutputFormatClass(TextOutputFormat.class);
        FileInputFormat.addInputPath(job2, new Path(args[1]+"/sameAsSecond"));
        FileOutputFormat.setOutputPath(job2, new Path(args[1]+"/Part2"));
		String[] m2 = { "elementIndexPart2"};
		for (String x : m2) {
			MultipleOutputs.addNamedOutput(job2, x, TextOutputFormat.class,
					Text.class, Text.class);

		}
       job2.waitForCompletion(true);
      
		
		return 0;
	}

	public static class Map extends Mapper<LongWritable, Text, Text, Text> {
		HashMap<String, Prefix> pr = new HashMap<String, Prefix>();

		@Override
		public void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			FileSplit fsplit = (FileSplit) context.getInputSplit();
			String filename = fsplit.getPath().getName();
			String split2[] = filename.split("_");
			String split[] = value.toString().split("\t");
			String datasetID = "";
			
			if (split.length >= 2) {
				context.write(new Text(split[0].trim()),
						new Text("SID\t"+split[1].trim()));
			}
			if (split.length == 1) {
				datasetID = split2[1].replace(".txt", "").replace(".ttl", "");
				String prefix = getPrefix(split[0]);
				if (pr.containsKey(prefix)
						&& pr.get(prefix).getNumberOfURIs() > 1) {
					context.write(new Text(split[0].trim()),
							new Text(datasetID));
				} else {
					context.getCounter(Counter.URISinONESOURCE).increment(1);
				}
			}
		}

	
	
	
		public String getPrefix(String word) {
			boolean https = false;
			String[] split;
			String prefix = "";
			if (word.startsWith("https://")) {
				word = word.replace("https://", "");
				https = true;
			} else {
				word = word.replace("http://", "");

			}
			split = word.split("/");
			if (split.length == 0)
				return null;
			if (split.length <= 1)
				return null;
			if (https == true) {
				prefix = "https://" + split[0];
			} else
				prefix = "http://" + split[0];
			return prefix;
		}

		@Override
		protected void setup(Context context) throws IOException {
			Configuration conf = context.getConfiguration();
			java.net.URI[] LocalPaths = null;
			try {
				LocalPaths = context.getCacheFiles();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			FileSystem fs = FileSystem.get(context.getConfiguration());

			FSDataInputStream out = fs.open(new Path(LocalPaths[0].getPath()));
			String line;
			BufferedReader br = null;

			br = new BufferedReader(new InputStreamReader(out));
			try {
				while ((line = br.readLine()) != null) {
					String split[] = line.split("\t");
					Prefix pref = new Prefix();
					pref.setId(Integer.parseInt(split[2]));
					pref.setIds(split[1]);
					pref.setUris(Integer.parseInt(split[3]));

					pr.put(split[0], pref);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}}

	public static class Reduce extends Reducer<Text, Text, Text, Text> {
		private MultipleOutputs<Text, Text> mos;

		@Override
		protected void setup(Context context) {
			Configuration conf = context.getConfiguration();
			mos = new MultipleOutputs<Text, Text>(context);
			// threshold=Double.parseDouble(conf.get("threshold"));
		}

		public void cleanup(Context context) throws IOException,
				InterruptedException {
			mos.close();
		}

		@Override
		public void reduce(Text key, Iterable<Text> values, Context context)
				throws IOException, InterruptedException {
			Set<Integer> docs = new TreeSet<Integer>();
			String SID="";
			for (Text val : values) {
				String[] split = val.toString().split("\t");
				if(val.toString().contains("SID")){
					SID=val.toString().split("\t")[1];
				}
				else
					docs.add(Integer.parseInt(val.toString()));
			}
			String datasetIDs = "";
			for (int z : docs)
				datasetIDs += z + ",";
			if (datasetIDs.length() > 0)
				datasetIDs = datasetIDs.substring(0, datasetIDs.length() - 1);
			if(!SID.equals("")){
				mos.write("sameAsSecond", SID, new Text(datasetIDs), "sameAsSecond/");
			
			}
			
			if (docs.size() >= 2) {
				if (SID.equals("")) {
				
					mos.write("elementIndexPart1", key, new Text(datasetIDs), "Part1/");
				}
			}
			else
				context.getCounter(Counter.URISPOLICY1).increment(1);
			
		}

	}
	
	
	public static class Map2 extends Mapper<LongWritable, Text, Text, Text> {
		HashMap<String, Prefix> pr = new HashMap<String, Prefix>();

		@Override
		public void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			
			String split[] = value.toString().split("\t");
			
			if (split.length >= 2) {
				context.write(new Text(split[0].trim()),
						new Text(split[1].trim()));
			}
		}

	}
	
	public static class Reduce2 extends Reducer<Text, Text, Text, Text> {
		private MultipleOutputs<Text, Text> mos;

		@Override
		protected void setup(Context context) {
			Configuration conf = context.getConfiguration();
			mos = new MultipleOutputs<Text, Text>(context);
			// threshold=Double.parseDouble(conf.get("threshold"));
		}

		public void cleanup(Context context) throws IOException,
				InterruptedException {
			mos.close();
		}

		@Override
		public void reduce(Text key, Iterable<Text> values, Context context)
				throws IOException, InterruptedException {
			Set<Integer> docs = new TreeSet<Integer>();
			for (Text val : values) {
				String[] split = val.toString().split(",");
				for(String g:split)
					docs.add(Integer.parseInt(g));
				

			}
			String datasetIDs = "";
			for (int z : docs)
				datasetIDs += z + ",";
			if (datasetIDs.length() > 0)
				datasetIDs = datasetIDs.substring(0, datasetIDs.length() - 1);
			if (docs.size() >= 2) {
					mos.write("elementIndexPart2", key, new Text(datasetIDs), "elementIndexPart2");	
			}
		}

	}
	

}

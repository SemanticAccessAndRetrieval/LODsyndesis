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

import gr.forth.ics.isl.preliminary.Prefix;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class CreateCommonLiteralsIndex extends Configured implements Tool {

	public static void main(String[] args) throws Exception {

		Configuration configuration = new Configuration();

		configuration.setBoolean("mapred.compress.map.output", true);
		configuration.setBoolean(
				"mapreduce.input.fileinputformat.input.dir.recursive", true);

		int res = ToolRunner.run(configuration,
				new CreateCommonLiteralsIndex(), args);

		System.exit(res);
	}

	public static enum Counter {
		Literals
	}

	@Override
	public int run(String[] args) throws Exception {
		Job job = new Job(getConf(), "CommonLiterals");
		job.setJarByClass(CreateCommonLiteralsIndex.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		job.setMapperClass(Map.class);
		job.setReducerClass(Reduce.class);

		job.setNumReduceTasks(Integer.parseInt(args[2]));
		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		job.waitForCompletion(true);

		return 0;
	}

	public void writeHDFSFile(String message, FileSystem fs) {
		Path filePath = new Path(
				"comparisons_number_inverted_index_approach.txt");
		try {
			FSDataOutputStream out = fs.create(filePath);
			out.writeUTF(message);
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static class Map extends Mapper<LongWritable, Text, Text, Text> {
		HashMap<String, Prefix> pr = new HashMap<String, Prefix>();
		HashMap<String, Integer> datasets = new HashMap<String, Integer>();

		@Override
		public void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			FileSplit fsplit = (FileSplit) context.getInputSplit();
			String cDataset = "";
			cDataset = fsplit.getPath().getName().replace(".txt", "")
					.replace(".ttl", "").split("_")[1];

			if (cDataset.contains("~"))
				return;

			String split[] = value.toString().split("\t");

			if (split.length >= 1) {
				String object = split[0];

				String key1 = "";
				if (object.length() >= 10)
					key1 = object.substring(0, 9);
				else
					key1 = object;
				context.write(new Text(key1), new Text(object + "\t" + cDataset
						+ "\t"));
			}
			// }
		}

	}

	public static class Reduce extends Reducer<Text, Text, Text, Text> {
		private MultipleOutputs<Text, Text> mos;

		@Override
		protected void setup(Context context) {
			Configuration conf = context.getConfiguration();
		}

		@Override
		public void reduce(Text key, Iterable<Text> values, Context context)
				throws IOException, InterruptedException {

			HashMap<String, Set<Integer>> litIndex = new HashMap<String, Set<Integer>>();
			for (Text val : values) {
				String[] split = val.toString().split("\t");
				String lit = split[0];
				if (!litIndex.containsKey(lit)) {
					Set<Integer> datasets = new TreeSet<Integer>();
					datasets.add(Integer.parseInt(split[1]));
					litIndex.put(lit, datasets);
				} else {
					litIndex.get(lit).add(Integer.parseInt(split[1]));
				}

			}

			for (java.util.Map.Entry<String, Set<Integer>> entry : litIndex
					.entrySet()) {
				Set<Integer> datasets = entry.getValue();
				String ids = "";
				if (datasets.size() > 1) {
					for (int x : datasets) {
						ids += x + ",";
					}
					ids = ids.substring(0, ids.length() - 1);
					context.write(new Text(entry.getKey()), new Text(ids));
					context.getCounter(Counter.Literals).increment(1);
				}

			}

		}
	}
}

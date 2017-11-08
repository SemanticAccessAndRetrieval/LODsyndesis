/*
This code belongs to the Semantic Access and Retrieval (SAR) group of the 
Information Systems Laboratory (ISL) of the 
Institute of Computer Science (ICS) of the  
Foundation for Research and Technology – Hellas (FORTH)

Nobody is allowed to use, copy, distribute, or modify this work.
It is published for reasons of research results reproducibility.

© 2017, Semantic Access and Retrieval group, All rights reserved

 */
package gr.forth.ics.isl.preliminary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
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

public class GetUniqueSameAs extends Configured implements Tool {

	public static void main(String[] args) throws Exception {

		Configuration configuration = new Configuration();

		configuration.setBoolean("mapred.compress.map.output", true);

		int res = ToolRunner.run(configuration, new GetUniqueSameAs(), args);

		System.exit(res);
	}

	public static enum Counter {
		SAMEASTRIPLES
	}

	@Override
	public int run(String[] args) throws Exception {
		Job job = new Job(getConf(), "GetUniqueSameAs");
		job.setJarByClass(GetUniqueSameAs.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		job.setMapperClass(Map.class);
		job.setReducerClass(Reduce.class);

		job.setNumReduceTasks(Integer.parseInt(args[3]));
		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		// job.setPartitionerClass(myPartitioner.class);
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		job.addCacheFile(new Path(args[2]).toUri());
		job.waitForCompletion(true);

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
			String docID = "";
			if (split[0].split("/").length <= 1) {
				return;
			}
			if (split2.length > 1) {
				docID = split2[1].replace(".txt", "").replace(".ttl", "");
				String prefix = getPrefix(split[0]);

				if (split.length == 2) {
					if (split[1].split("/").length <= 1) {
						return;
					}
					if ((value.toString().contains("purl.org")
							|| value.toString().contains("www.w3.org") || value
							.toString().contains("xmlns.com/foaf"))) {
						return;
					}
					if (!split[0].trim().equals(split[1].trim())) {
						String prefix2 = getPrefix(split[1]);
						if (pr.containsKey(prefix)) {
							if (split[0].trim().compareTo(split[1].trim()) > 0)
								context.write(new Text(split[0].trim() + "\t"
										+ split[1].trim()), new Text(""));
							else
								context.write(new Text(split[1].trim() + "\t"
										+ split[0].trim()), new Text(""));
							return;
						} else if (pr.containsKey(prefix2)) {

							if (split[0].trim().compareTo(split[1].trim()) > 0)
								context.write(new Text(split[0].trim() + "\t"
										+ split[1].trim()), new Text(""));
							else
								context.write(new Text(split[1].trim() + "\t"
										+ split[0].trim()), new Text(""));
							return;
						}

					}
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

			File pre = new File(LocalPaths[0].getPath());
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

		}

	}

	public static class myPartitioner extends Partitioner<Text, Text> {

		@Override
		public int getPartition(Text key, Text value, int arg2) {
			// TODO Auto-generated method stub
			String[] split = value.toString().split("\t");
			int id = Integer.parseInt(split[0]);

			if ((id / arg2) % 2 == 0) {
				return id % arg2;
			} else
				return ((arg2 - 1) - id % arg2);
		}

	}

	public static class Reduce extends Reducer<Text, Text, Text, NullWritable> {
		private MultipleOutputs<Text, Text> mos;

		@Override
		protected void setup(Context context) {
			Configuration conf = context.getConfiguration();
			// threshold=Double.parseDouble(conf.get("threshold"));
		}

		public void cleanup(Context context) throws IOException,
				InterruptedException {

		}

		@Override
		public void reduce(Text key, Iterable<Text> values, Context context)
				throws IOException, InterruptedException {
			context.write(key, NullWritable.get());
			context.getCounter(Counter.SAMEASTRIPLES).increment(1);

		}

	}
}

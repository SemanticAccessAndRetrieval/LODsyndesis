/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gr.forth.ics.isl.latticeCreation;

import java.io.IOException;
import java.util.HashMap;
import java.util.TreeMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
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

public class CreateLattice extends Configured implements Tool {

	public static void main(String[] args) throws Exception {
		Configuration configuration = new Configuration();

		configuration.setBoolean("mapred.compress.map.output", true);
		configuration.setBoolean(
				"mapreduce.input.fileinputformat.input.dir.recursive", true);
		configuration.setBoolean(
				"mapreduce.input.fileinputformat.input.dir.recursive", true);
		int res = ToolRunner.run(configuration, new CreateLattice(), args);

		System.exit(res);
	}

	public static enum Counter {
		subsets;
	}

	@Override
	public int run(String[] args) throws Exception {
		getConf().set("threshold", args[3]);
		getConf().set("maxLevel", args[4]);
		getConf().set("printFrom", args[5]);
		getConf().set("printTo", args[6]);
		getConf().set("distance", args[7]);
		Job job = new Job(getConf(), "LatticeConfigured");
		job.setJarByClass(CreateLattice.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		job.setMapperClass(Map.class);
		job.setReducerClass(Reduce.class);
		// System.out.println("high");

		job.setNumReduceTasks(Integer.parseInt(args[2]));
		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		// job.setPartitionerClass(myPartitioner.class);
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		job.waitForCompletion(true);
		String x = "Lattice";
		Job job2 = new Job(getConf(), x);
		job2.setJarByClass(CreateLattice.class);
		job2.setOutputKeyClass(Text.class);
		job2.setOutputValueClass(Text.class);
		job2.setMapperClass(Map2.class);
		job2.setReducerClass(Reduce2.class);

		job2.setNumReduceTasks(Integer.parseInt(args[2]));
		job2.setInputFormatClass(TextInputFormat.class);
		job2.setOutputFormatClass(TextOutputFormat.class);
		FileInputFormat.addInputPath(job2, new Path(args[1]));
		FileOutputFormat.setOutputPath(job2, new Path(args[1] + "Print"));
		job2.waitForCompletion(true);
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
		HashMap<String, String> uris = new HashMap<String, String>();
		HashMap<String, Integer> datasets = new HashMap<String, Integer>();
		int currentID = -1;

		@Override
		public void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {

			String[] split = value.toString().split("\t");
			String[] line = split[0].split(",");

			for (int i = 0; i < line.length - 1; i++) {
				String word = line[i];
				for (int j = i + 1; j < line.length; j++) {
					word = line[i] + "," + line[j];
					String rest = "";
					for (int k = j + 1; k < line.length; k++) {
						rest += line[k];
						if (k + 1 != line.length) {
							rest += ",";
						}
					}
					context.write(new Text(word), new Text(rest + "@"
							+ split[1]));

				}
			}

		}

		@Override
		protected void setup(Context context) throws IOException {

		}

	}

	public static class Map2 extends Mapper<LongWritable, Text, Text, Text> {
		@Override
		public void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			TreeMap<String, HashMap<String, Integer>> des = new TreeMap<String, HashMap<String, Integer>>();
			String[] split = value.toString().split("\t");

			String x = value.toString().replace(split[0] + "\t", "");

			String[] line = x.split("\t");
			HashMap<String, Integer> desc = new HashMap<>();
			int score = 0;
			for (String subset : line) {

				if (subset.equals(""))
					continue;

				int cScore = 0;
				if (subset.toString().split("@").length == 1) {
					cScore = Integer.parseInt(subset.toString().split("@")[0]);
					score += cScore;
					continue;
				} else
					cScore = Integer.parseInt(subset.toString().split("@")[1]);
				score += cScore;
				if (desc.containsKey(subset.toString().split("@")[0]))
					desc.put(subset.toString().split("@")[0],
							desc.get(subset.toString().split("@")[0]) + cScore);
				else
					desc.put(subset.toString().split("@")[0], cScore);
				String[] u = subset.toString().split("@")[0].split(",");
				if (u.length > 0 && !u[0].trim().equals("")) {
					for (int i = 0; i < u.length; i++) {

						if (u[i].startsWith("@")) {

							continue;
						}
						String word = split[0].toString() + "," + u[i];
						if (Integer.parseInt(u[i].trim()) >= 10) {
							word = split[0].toString() + ",a" + u[i];
						}
						String sbs = "";
						int cc = 0;
						sbs = new String(word);
						String rest = "";
						for (int j = i + 1; j < u.length; j++) {
							if (u[j].contains("@"))
								continue;
							rest += u[j];
							if (j + 1 != u.length) {
								rest += ",";
							}
						}

						if (!des.containsKey(word)) {
							des.put(word, new HashMap<String, Integer>());
						}
						if (des.get(word).containsKey(rest)) {
							des.get(word).put(rest,
									des.get(word).get(rest) + cScore);
						} else {
							des.get(word).put(rest, cScore);
						}
					}
				}
			}
			context.getCounter(Counter.subsets).increment(1);
			int size = split[0].split(",").length;
			int last = Integer.parseInt(split[0].split(",")[size - 1]);
			double distance = 1 / Math.pow(2, last);
			for (String f : des.keySet()) {
				String s = f.replaceAll("a", "");

				if (distance <= dist) {
					String desce = "";
					for (java.util.Map.Entry<String, Integer> entry : desc
							.entrySet()) {

						desce += entry.getKey() + "@" + entry.getValue() + "\t";
					}

					context.write(new Text(split[0]), new Text(desce + "#" + s));

					break;
				} else {
					distance = distance
							- recursive(2, null, null, s, des.get(f), false,
									null, context);
				}
			}

		}

		static int maxLevel;
		static int threshold, printFrom, printTo;
		static Double dist;

		@Override
		protected void setup(Context context) {
			Configuration conf = context.getConfiguration();
			threshold = Integer.parseInt(conf.get("threshold"));
			dist = Double.parseDouble(conf.get("distance"));
			maxLevel = Integer.parseInt(conf.get("maxLevel"));
			printFrom = Integer.parseInt(conf.get("printFrom"));
			printTo = Integer.parseInt(conf.get("printTo"));
		}

		public static double recursive(int level, java.util.Set<String> sub,
				String fName, String node, java.util.Map<String, Integer> desc,
				boolean whole, java.util.ArrayList<String> ret, Context context)
				throws IOException, InterruptedException {

			context.getCounter(Counter.subsets).increment(1);
			level++;
			String[] x = node.split(",");
			int size = x.length;
			int last = Integer.parseInt(x[size - 1]);
			double distance = 1 / Math.pow(2, last);// 5 1,2,3
			double iDistance = new Double(distance);
			// System.out.println(node+" "+distance);
			int score = 0;

			for (String subset : desc.keySet()) {
				int cScore = desc.get(subset);
				score += cScore;

			}

			if (level <= maxLevel && score >= threshold) {
				TreeMap<String, HashMap<String, Integer>> des = new TreeMap<String, HashMap<String, Integer>>();
				for (String subset : desc.keySet()) {

					int cScore = desc.get(subset);
					String[] u = subset.split(",");

					if (u.length > 0 && !u[0].trim().equals("")) {
						for (int i = 0; i < u.length; i++) {
							String word = node + "," + u[i];
							if (Integer.parseInt(u[i].trim()) >= 10) {
								word = node + ",a" + u[i];
							}
							int cc = 0;
							String sbs = new String(word);
							String rest = "";
							for (int j = i + 1; j < u.length; j++) {
								rest += u[j];
								if (j + 1 != u.length) {
									rest += ",";
								}
							}

							if (!des.containsKey(word)) {
								des.put(word, new HashMap<String, Integer>());
							}
							if (des.get(word).containsKey(rest)) {
								des.get(word).put(rest,
										des.get(word).get(rest) + cScore);
							} else {
								des.get(word).put(rest, cScore);
							}
						}
					}
				}
				for (String f : des.keySet()) {
					String s = f.replaceAll("a", "");
					if (distance <= dist) {

						String desce = "";
						for (java.util.Map.Entry<String, Integer> entry : desc
								.entrySet()) {

							desce += entry.getKey() + "@" + entry.getValue()
									+ "\t";

						}
						context.write(new Text(node), new Text(desce + "#" + s));
						break;
					} else
						distance = distance
								- recursive(level, null, null, s, des.get(f),
										false, null, context);
				}

			}
			return iDistance;
		}

	}

	public static class Reduce extends Reducer<Text, Text, Text, Text> {
		private MultipleOutputs<Text, Text> mos;
		static int maxLevel;
		static int threshold, printFrom, printTo;

		@Override
		protected void setup(Context context) {
			Configuration conf = context.getConfiguration();
			mos = new MultipleOutputs<Text, Text>(context);
			threshold = Integer.parseInt(conf.get("threshold"));
			maxLevel = Integer.parseInt(conf.get("maxLevel"));
			printFrom = Integer.parseInt(conf.get("printFrom"));
			printTo = Integer.parseInt(conf.get("printTo"));
		}

		public void cleanup(Context context) throws IOException,
				InterruptedException {
			mos.close();
		}

		@Override
		public void reduce(Text key, Iterable<Text> values, Context context)
				throws IOException, InterruptedException {
			String out = "";
			for (Text subset : values) {
				out += subset + "\t";

			}
			context.write(new Text(key.toString()), new Text(out + ""));

		}

	}

	public static class Reduce2 extends Reducer<Text, Text, Text, Text> {
		private MultipleOutputs<Text, Text> mos;
		static int maxLevel, levelsp;
		static int threshold, printFrom, printTo;

		@Override
		protected void setup(Context context) {
			Configuration conf = context.getConfiguration();
			mos = new MultipleOutputs<Text, Text>(context);
			threshold = Integer.parseInt(conf.get("threshold"));
			maxLevel = Integer.parseInt(conf.get("maxLevel"));
			printFrom = Integer.parseInt(conf.get("printFrom"));
			printTo = Integer.parseInt(conf.get("printTo"));
		}

		public void cleanup(Context context) throws IOException,
				InterruptedException {
			mos.close();
		}

		@Override
		public void reduce(Text key, Iterable<Text> values, Context context)
				throws IOException, InterruptedException {
			TreeMap<String, HashMap<String, Integer>> des = new TreeMap<String, HashMap<String, Integer>>();
			int score = 0;
			String sub = "";
			for (Text subset : values) {
				sub = subset.toString();
			}
			String[] split1 = sub.split("#");
			String[] split = split1[0].split("\t");
			String start = split1[1];
			for (String subset : split) {

				int cScore = 0;
				if (subset.toString().split("@").length == 1) {
					cScore = Integer.parseInt(subset.toString().split("@")[0]);
					score += cScore;
					continue;
				} else
					cScore = Integer.parseInt(subset.toString().split("@")[1]);
				score += cScore;
				String[] u = subset.toString().split("@")[0].split(",");
				if (u.length > 0 && !u[0].trim().equals("")) {
					for (int i = 0; i < u.length; i++) {
						String word = key.toString() + "," + u[i];
						if (Integer.parseInt(u[i].trim()) >= 10) {
							word = key.toString() + ",a" + u[i];
						}
						String sbs = "";
						sbs = new String(word);
						String rest = "";
						for (int j = i + 1; j < u.length; j++) {
							rest += u[j];
							if (j + 1 != u.length) {
								rest += ",";
							}
						}

						if (!des.containsKey(word)) {
							des.put(word, new HashMap<String, Integer>());
						}
						if (des.get(word).containsKey(rest)) {
							des.get(word).put(rest,
									des.get(word).get(rest) + cScore);
						} else {
							des.get(word).put(rest, cScore);
						}
					}
				}
			}
			int level = key.toString().split(",").length;
			if (printFrom >= 0 && printTo >= level && score >= threshold) {
				context.write(new Text(key.toString()), new Text(score + ""));
			}

			boolean st = false;

			for (String f : des.keySet()) {
				String s = f.replaceAll("a", "");
				// System.out.println(s+" "+start );
				if (s.equals(start) || st == true) {
					st = true;
					recursive(level, null, null, s, des.get(f), false, null,
							context);
				}
			}

		}

		public static void recursive(int level, java.util.Set<String> sub,
				String fName, String node, java.util.Map<String, Integer> desc,
				boolean whole, java.util.ArrayList<String> ret, Context context)
				throws IOException, InterruptedException {
			context.getCounter(Counter.subsets).increment(1);

			level++;
			int score = 0;
			for (String subset : desc.keySet()) {
				int cScore = desc.get(subset);
				score += cScore;

			}

			if (printFrom >= 0 && printTo >= level && score >= threshold) {
				context.write(new Text(node), new Text(score + ""));
			}

			if (level <= maxLevel && score >= threshold) {

				HashMap<String, HashMap<String, Integer>> des = new HashMap<String, HashMap<String, Integer>>();
				for (String subset : desc.keySet()) {

					int cScore = desc.get(subset);
					String[] u = subset.split(",");
					String already = "";
					if (u.length > 0 && !u[0].trim().equals("")) {
						for (int i = 0; i < u.length; i++) {
							String word = node + "," + u[i];

							int cc = 0;
							String sbs = new String(word);
							String rest = "";
							for (int j = i + 1; j < u.length; j++) {
								rest += u[j];
								if (j + 1 != u.length) {
									rest += ",";
								}
							}

							if (!des.containsKey(word)) {
								des.put(word, new HashMap<String, Integer>());
							}
							if (des.get(word).containsKey(rest)) {
								des.get(word).put(rest,
										des.get(word).get(rest) + cScore);
							} else {
								des.get(word).put(rest, cScore);
							}
						}
					}
				}
				desc.clear();
				for (String s : des.keySet()) {
					recursive(level, null, null, s, des.get(s), false, null,
							context);
				}

			}
		}

	}

}
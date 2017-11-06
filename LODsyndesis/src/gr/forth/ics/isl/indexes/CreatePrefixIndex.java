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
package gr.forth.ics.isl.indexes;

import gr.forth.ics.isl.preliminary.MapComparator;
import gr.forth.ics.isl.preliminary.Prefix;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class CreatePrefixIndex extends Configured implements Tool {

	public static void main(String[] args) throws Exception {

		Configuration configuration = new Configuration();

		configuration.setBoolean("mapred.compress.map.output", true);

		int res = ToolRunner.run(configuration, new CreatePrefixIndex(), args);

		System.exit(res);
	}

	public static enum Counter {
		UNIQUEURIS
	}

	@Override
	public int run(String[] args) throws Exception {
		Job job = new Job(getConf(), "PrefixIndex");
		job.setJarByClass(CreatePrefixIndex.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		job.setMapperClass(Map.class);
		job.setReducerClass(Reduce.class);
		job.setNumReduceTasks(Integer.parseInt(args[2]));
		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		String[] m = { "sameAsPrefix", "prefixIndex" };
		for (String x : m) {
			MultipleOutputs.addNamedOutput(job, x, TextOutputFormat.class,
					Text.class, Text.class);

		}
		job.waitForCompletion(true);

		return 0;
	}

	public static class Map extends Mapper<LongWritable, Text, Text, Text> {
		HashMap<String, TreeMap<Integer, Integer>> prefixIndex = new HashMap<String, TreeMap<Integer, Integer>>();
		HashMap<String, Integer> sameAs = new HashMap<String, Integer>();

		@Override
		public void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			FileSplit fsplit = (FileSplit) context.getInputSplit();
			String filename = fsplit.getPath().getName();
			String split2[] = filename.split("_");
			String split[] = value.toString().split("\t");
			int datasetID = Integer.parseInt(split2[1].replace(".txt", ""));
			if (split[0].split("/").length <= 1) {
				return;
			}
			String prefix = getPrefix(split[0]);

			if (prefixIndex.containsKey(prefix)) {
				if (prefixIndex.get(prefix).containsKey(datasetID))
					prefixIndex.get(prefix).put(datasetID,
							prefixIndex.get(prefix).get(datasetID) + 1);
				else
					prefixIndex.get(prefix).put(datasetID, 1);
				;
			} else {
				TreeMap<Integer, Integer> tm = new TreeMap<Integer, Integer>();
				tm.put(datasetID, 1);
				prefixIndex.put(prefix, tm);
			}

			if (split.length == 2) {
				if (split[1].split("/").length <= 1) {
					return;
				}
				String prefix2 = getPrefix(split[1]);
				if (prefixIndex.containsKey(prefix2)) {
					if (prefixIndex.get(prefix2).containsKey(datasetID))
						prefixIndex.get(prefix2).put(datasetID,
								prefixIndex.get(prefix2).get(datasetID) + 1);
					else
						prefixIndex.get(prefix2).put(datasetID, 1);
				} else {
					TreeMap<Integer, Integer> tm = new TreeMap<Integer, Integer>();
					tm.put(datasetID, 1);
					prefixIndex.put(prefix2, tm);
				}
				if (sameAs.containsKey(prefix)) {
					sameAs.put(prefix, sameAs.get(prefix) + 1);
				} else {
					sameAs.put(prefix, 1);
				}
				if (sameAs.containsKey(prefix2)) {
					sameAs.put(prefix2, sameAs.get(prefix2) + 1);
				} else {
					sameAs.put(prefix2, 1);
				}
			} else {
				context.getCounter(Counter.UNIQUEURIS).increment(1);
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
			if (https == true) {
				prefix = "https://" + split[0];
			} else
				prefix = "http://" + split[0];
			return prefix;
		}

		@Override
		public void cleanup(Context context) throws IOException,
				InterruptedException {
			for (String p : prefixIndex.keySet()) {
				String value = "";
				for (int k : prefixIndex.get(p).keySet()) {
					value += k + ":" + prefixIndex.get(p).get(k) + "\t";
				}
				value = value.substring(0, value.length() - 1);
				context.write(new Text(p), new Text(value));
			}
			for (String p : sameAs.keySet()) {
				String value = sameAs.get(p).toString();
				context.write(new Text("sameAs:" + p), new Text(value));
			}
		}

	}

	public static class Reduce extends Reducer<Text, Text, Text, Text> {
		private MultipleOutputs<Text, Text> mos;
		HashMap<Prefix, Integer> prefixIndex = new HashMap<Prefix, Integer>();
		HashMap<String, TreeMap<Integer, Integer>> prefixIndex2 = new HashMap<String, TreeMap<Integer, Integer>>();
		HashMap<String, Integer> sameAs = new HashMap<String, Integer>();
		MapComparator vc = new MapComparator(prefixIndex);
		TreeMap<Prefix, Integer> sortedPrefix = new TreeMap<Prefix, Integer>(vc);

		@Override
		protected void setup(Context context) {
			Configuration conf = context.getConfiguration();
			mos = new MultipleOutputs<Text, Text>(context);
		}

		public void cleanup(Context context) throws IOException,
				InterruptedException {
			sortedPrefix.putAll(prefixIndex);
			int count = 0;
			for (Prefix p : sortedPrefix.keySet()) {
				String value = "";
				if (prefixIndex2.containsKey(p.getName())) {
					for (int pr : prefixIndex2.get(p.getName()).keySet()) {
						value += pr + ":"
								+ prefixIndex2.get(p.getName()).get(pr) + ",";
					}
				}
				mos.write("prefixIndex", p.getName(),
						new Text(value.substring(0, value.length() - 1) + "\t"
								+ count + "\t" + p.getUris()),
						"prefixIndex/prefixIndex.txt");
				count++;
			}
			mos.close();
		}

		@Override
		public void reduce(Text key, Iterable<Text> values, Context context)
				throws IOException, InterruptedException {
			if (key.toString().startsWith("sameAs:")) {
				String myKey = key.toString().replace("sameAs:", "");
				int freq = 0;
				for (Text val : values) {
					freq += Integer.parseInt(val.toString());
				}
				myKey = myKey.replace("@", "");
				mos.write("sameAsPrefix", myKey,
						new Text(Integer.toString(freq)),
						"sameAsPrefix/sameAsPrefix.txt");
			} else {
				int freq = 0;
				Prefix pref = new Prefix();
				Set<Integer> datasetIDs = new TreeSet<Integer>();
				for (Text val : values) {
					String[] split = val.toString().split("\t");
					for (String f : split) {

						String[] ids = split[0].split(":");
						datasetIDs.add(Integer.parseInt(ids[0]));
						freq += Integer.parseInt(ids[1]);
						if (prefixIndex2.containsKey(key.toString())) {
							if (prefixIndex2.get(key.toString()).containsKey(
									Integer.parseInt(ids[0])))
								prefixIndex2.get(key.toString()).put(
										Integer.parseInt(ids[0]),
										prefixIndex2.get(key.toString()).get(
												Integer.parseInt(ids[0]))
												+ Integer.parseInt(ids[1]));
							else
								prefixIndex2.get(key.toString()).put(
										Integer.parseInt(ids[0]),
										Integer.parseInt(ids[1]));
						} else {
							TreeMap<Integer, Integer> tm = new TreeMap<Integer, Integer>();
							tm.put(Integer.parseInt(ids[0]),
									Integer.parseInt(ids[1]));
							prefixIndex2.put(key.toString(), tm);
						}
					}

				}
				String writeIDs = "";
				for (int i : datasetIDs) {
					writeIDs += i + ",";
				}
				pref.setName(key.toString());
				if (writeIDs.length() == 0)
					return;
				writeIDs = writeIDs.substring(0, writeIDs.length() - 1);
				pref.setUris(freq);
				pref.setIds(writeIDs);
				prefixIndex.put(pref, freq);
			}

		}

	}

}

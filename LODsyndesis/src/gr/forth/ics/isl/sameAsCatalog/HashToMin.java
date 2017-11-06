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
package gr.forth.ics.isl.sameAsCatalog;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
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


public class HashToMin extends Configured implements Tool {
	public static int idOfChain = 0;

	public static void main(String[] args) throws Exception {

		Configuration configuration = new Configuration();

		configuration.setBoolean("mapred.compress.map.output", true);

		int res = ToolRunner.run(configuration, new HashToMin(), args);

		System.exit(res);
	}

	public static enum Counter {
		REMAININGURIS, ID, COUNT, CLASSESOFEQUIVALENCE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, MORE
	}

	@Override
	public int run(String[] args) throws Exception {
		int count = 1;

		boolean oneReducer = false;
		while (true) {
			String jobid = "" + count;
			if (count < 10)
				jobid = "0" + "" + Integer.toString(count);
			getConf().set("job", jobid);
			int type = Integer.parseInt(args[5]);
			getConf().set("type", type + "");
			Job job = new Job(getConf(), "HashMin");
			job.setJarByClass(HashToMin.class);
			job.setOutputKeyClass(Text.class);
			job.setOutputValueClass(Text.class);

			job.setMapperClass(Map.class);

			job.setInputFormatClass(TextInputFormat.class);
			job.setOutputFormatClass(TextOutputFormat.class);
			;
			if (count == 1)
				FileInputFormat.addInputPath(job, new Path(args[0]));
			else
				FileInputFormat.addInputPath(job, new Path(args[1]+"/sameAs"
						+ (count - 1) + "/sameAsP"));
			int threshold = Integer.parseInt(args[4]);

			FileOutputFormat.setOutputPath(job, new Path(args[1]+"/sameAs"
					+ count));
			job.addCacheFile(new Path(args[2]).toUri());
			String[] m = { "sameAsCatalog", "sameAsP", "sameAsChain" };

			for (String x : m) {
				MultipleOutputs.addNamedOutput(job, x, TextOutputFormat.class,
						Text.class, Text.class);
			}
			if (oneReducer) {
				job.setReducerClass(SameAs.class);
				job.setNumReduceTasks(1);
			} else {
				job.setReducerClass(Reduce.class);
				job.setNumReduceTasks(Integer.parseInt(args[3]));
			}

			job.waitForCompletion(true);

			org.apache.hadoop.mapreduce.Counter myCounter = job.getCounters()
					.findCounter(Counter.REMAININGURIS);
			org.apache.hadoop.mapreduce.Counter myCounter2 = job.getCounters()
					.findCounter(Counter.CLASSESOFEQUIVALENCE);
			count++;
			if (myCounter.getValue() == 0)
				break;
			if (myCounter.getValue() < threshold) {
				oneReducer = true;
			}

		}
		return 0;
	}

	public void writeHDFSFile(String message, FileSystem fs, int count,
			String name) {
		Path filePath = new Path(name + count + ".txt");
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
		HashMap<String, Integer> pr = new HashMap<String, Integer>();
		int type = 0;

		@Override
		public void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			FileSplit fsplit = (FileSplit) context.getInputSplit();
			String filename = fsplit.getPath().getName();
			if (!filename.contains("sas")) {
				String[] split = value.toString().split("\t");
				if (split.length == 2)
					context.write(new Text(split[0]), new Text(split[1]));
				else {
					String keyR = split[0];
					String valR = split[1] + "\t" + split[2];
					if (split.length > 3)
						valR += "\t" + split[3];
					context.write(new Text(keyR), new Text(valR));
				}
			} else {
				String split[] = value.toString().split("\t");

				String min = split[0];
				String minPrefix = getPrefix(min);
				String allURIs = "";
				String[] other = split[1].split(",");
				HashMap<String, Integer> edgeCount = new HashMap<String, Integer>();
				edgeCount.put(split[0], other.length);
				for (String x : other) {
					edgeCount.put(x, -1);
					String prefix = getPrefix(x);
					if (prefix.equals(minPrefix)) {
						if (min.compareTo(x) > 0) {
							min = x;
						}
					} else if (pr.containsKey(minPrefix)
							&& pr.containsKey(prefix)) {
						if (pr.get(minPrefix).intValue() == pr.get(prefix)
								.intValue()) {
							if (minPrefix.compareTo(prefix) > 0) {
								min = x;
								minPrefix = prefix;
							}
						} else if (pr.get(minPrefix).intValue() < pr
								.get(prefix).intValue()) {
							min = x;
							minPrefix = prefix;
						}
					}
				}
				min = min.replace("@", "papaki");
				for (String str : edgeCount.keySet()) {
					int degree = edgeCount.get(str);

					String val = str;
					val = val.replace("@", "papaki");
					allURIs += val + "@" + degree + ",";
					if (!val.equals(min)) {
						context.write(new Text(val), new Text("Min:" + min));
					}
				}
				if (allURIs.endsWith(","))
					allURIs = allURIs.substring(0, allURIs.length() - 1);

				if (split.length == 3)
					context.write(new Text(min), new Text(allURIs + "\t"
							+ split[2]));
				else
					context.write(new Text(min), new Text(allURIs + "\t-1"));
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
			if (https == true) {
				prefix = "https://" + split[0];
			} else
				prefix = "http://" + split[0];
			return prefix;
		}

		@Override
		protected void setup(Context context) throws IOException {
			Configuration conf = context.getConfiguration();
			type = Integer.parseInt(conf.get("type"));
			java.net.URI[] LocalPaths = null;
			try {
				LocalPaths = context.getCacheFiles();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String line;
			FileSystem fs = FileSystem.get(context.getConfiguration());
			FSDataInputStream out = fs.open(new Path(LocalPaths[0].getPath()));
			BufferedReader br = null;

			br = new BufferedReader(new InputStreamReader(out));

			try {
				while ((line = br.readLine()) != null) {
					String split[] = line.split("\t");
					int freq = 0;
					if (type != 0) {
						if (type == 2)
							freq = Integer.parseInt(split[3]);
						else
							freq = Integer.parseInt(split[1]);
					}

					pr.put(split[0], freq);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	public static class Reduce extends Reducer<Text, Text, Text, Text> {
		private MultipleOutputs<Text, Text> mos;
		HashMap<String, Integer> pr = new HashMap<String, Integer>();
		String code = "";
		int ID = 0, type = 0;

		public String getPrefix(String URI) {
			boolean https = false;
			String[] split;
			String prefix = "";
			if (URI.startsWith("https://")) {
				URI = URI.replace("https://", "");
				https = true;
			} else {
				URI = URI.replace("http://", "");

			}
			split = URI.split("/");
			if (https == true) {
				prefix = "https://" + split[0];
			} else
				prefix = "http://" + split[0];
			return prefix;
		}

		@Override
		protected void setup(Context context) throws IOException {
			Configuration conf = context.getConfiguration();
			mos = new MultipleOutputs<Text, Text>(context);
			int id = context.getTaskAttemptID().getTaskID().getId();
			String finalID = Integer.toString(id);
			if (id < 10) {
				finalID = "0" + Integer.toString(id);
			}
			if (conf.get("job") != null)
				code = conf.get("job") + finalID;
			else
				code = "00" + finalID;
			type = Integer.parseInt(conf.get("type"));
			java.net.URI[] LocalPaths = null;
			try {
				LocalPaths = context.getCacheFiles();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String line;
			FileSystem fs = FileSystem.get(context.getConfiguration());
			FSDataInputStream out = fs.open(new Path(LocalPaths[0].getPath()));
			BufferedReader br = null;

			br = new BufferedReader(new InputStreamReader(out));

			try {
				while ((line = br.readLine()) != null) {
					String split[] = line.split("\t");

					int freq = 0;
					if (type != 0) {
						if (type == 2)
							freq = Integer.parseInt(split[3]);
						else
							freq = Integer.parseInt(split[1]);
					}

					pr.put(split[0], freq);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		public void cleanup(Context context) throws IOException,
				InterruptedException {
			mos.close();
		}

		@Override
		public void reduce(Text key, Iterable<Text> values, Context context)
				throws IOException, InterruptedException {
			Set<Integer> datasets = new TreeSet<Integer>();
			Set<String> alluris = new HashSet<String>();
			int finishedCount = 0;
			String tempMin = "";
			String minPre = "";
			boolean noValues = true;
			Set<String> finishedURIs = new HashSet<String>();
			TreeMap<String, Integer> equivClasses = new TreeMap<String, Integer>();
			for (Text val : values) {
				if (val.toString().startsWith("Remove")) {
					return;
				}
				if (val.toString().startsWith("Min:")) {
					alluris.add(val.toString().replace("Min:", ""));
					String tMin = val.toString().replace("Min:", "");
					String prefix = getPrefix(tMin);
					if (tempMin == "") {
						tempMin = tMin;
						minPre = getPrefix(tMin);
					} else if (prefix.equals(minPre)) {
						if (tempMin.compareTo(tMin) > 0) {
							tempMin = tMin;
						}
					} else if (pr.containsKey(minPre) && pr.containsKey(prefix)) {
						if (pr.get(minPre).intValue() == pr.get(prefix)
								.intValue()) {
							if (minPre.compareTo(prefix) > 0) {
								tempMin = tMin;
								minPre = prefix;
							}
						} else if (pr.get(minPre).intValue() < pr.get(prefix)
								.intValue()) {
							tempMin = tMin;
							minPre = prefix;
						}
					}
					continue;
				}
				noValues = false;
				String[] split = val.toString().split("\t");
				String[] uris = split[0].split(",");
				for (String u : uris) {
					String[] splitDegree = u.split("@");
					if (splitDegree.length != 2 || splitDegree[1] == null)
						continue;
					int degree = Integer.parseInt(splitDegree[1]);
					u = splitDegree[0];
					if (equivClasses.containsKey(u))
						equivClasses.put(u, equivClasses.get(u) + degree);
					else
						equivClasses.put(u, new Integer(degree));
				}

				String[] ids = split[1].split(",");
				for (String u : ids) {
					if (StringUtils.isNumeric(u) && !u.equals("")
							&& !u.equals("-1"))
						datasets.add(Integer.parseInt(u));
				}
				if (split.length == 3) {
					String[] fin = split[2].split(",");
					for (String u : fin) {
						finishedURIs.add(u);
						alluris.add(u);
					}
				}
			}
			if (noValues) {
				for (String u : alluris) {
					if (!u.equals(tempMin))
						mos.write("sameAsP", new Text(u), new Text("Min:"
								+ tempMin), "sameAsP/");
				}

				return;
			}
			String finished = "";
			for (String fin : finishedURIs) {
				equivClasses.put(fin, 0);
				finished += fin + ",";
				finishedCount++;
			}
			String ids = "";
			for (int s : datasets) {
				ids += s + ",";
			}
			if (ids.length() > 0)
				ids = ids.substring(0, ids.length() - 1);
			String sameAsURIs = "";

			String min = "", minPrefix = "";
			boolean fin = true;
			for (String s : equivClasses.keySet()) {
				alluris.add(s);
				String prefix = getPrefix(s);
				if (equivClasses.get(s) != 0) {
					sameAsURIs += s + "@" + equivClasses.get(s) + ",";
					fin = false;
				}

				if (min == "") {
					min = s;
					minPrefix = getPrefix(s);
				} else if (prefix.equals(minPrefix)) {
					if (min.compareTo(s) > 0) {
						min = s;
					}
				} else if (pr.containsKey(minPrefix) && pr.containsKey(prefix)) {
					if (pr.get(minPrefix).intValue() == pr.get(prefix)
							.intValue()) {
						if (minPrefix.compareTo(prefix) > 0) {
							min = s;
							minPrefix = prefix;
						}
					} else if (pr.get(minPrefix).intValue() < pr.get(prefix)
							.intValue()) {
						min = s;
						minPrefix = prefix;
					}
				}

				if (equivClasses.get(s) == 0) {
					finished += s + ",";
					finishedCount++;
				}
			}
			if (fin)
				min = "";
			if (finished.length() > 0) {
				finished = finished.substring(0, finished.length() - 1);
			}
			if (sameAsURIs.length() > 0)
				sameAsURIs = sameAsURIs.substring(0, sameAsURIs.length() - 1);

			if (min == "") {
				String codeID = ID + code;
				ID++;
				mos.write("sameAsChain",
						codeID + "\t" + finished.replace("papaki", "@"),
						new Text(ids), "sameAsChain/");

				HashSet<String> finishURIs = new HashSet<String>();
				for (String uri : finished.split(",")) {
					finishURIs.add(uri.trim());
				}
				for (String k : finishURIs) {

					mos.write("sameAsCatalog",
							k.replace("comma", ",").replace("papaki", "@"),
							codeID, "sameAsCatalog/");
				}
				context.getCounter(Counter.CLASSESOFEQUIVALENCE).increment(1);

				for (String u : alluris) {
					if (!u.equals(min))
						mos.write("sameAsP", new Text(u), new Text("Remove"),
								"sameAsP/");
				}
				int eSize = finishedCount;
				if (eSize == 2) {
					context.getCounter(Counter.TWO).increment(1);

				} else if (eSize == 3)
					context.getCounter(Counter.THREE).increment(1);
				else if (eSize == 4)
					context.getCounter(Counter.FOUR).increment(1);
				else if (eSize == 5)
					context.getCounter(Counter.FIVE).increment(1);
				else if (eSize == 6)
					context.getCounter(Counter.SIX).increment(1);
				else if (eSize == 7)
					context.getCounter(Counter.SEVEN).increment(1);
				else if (eSize == 8)
					context.getCounter(Counter.EIGHT).increment(1);
				else if (eSize == 9)
					context.getCounter(Counter.NINE).increment(1);
				else if (eSize == 10)
					context.getCounter(Counter.TEN).increment(1);
				else if (eSize >= 11)
					context.getCounter(Counter.MORE).increment(1);
			} else {
				if (ids == "")
					ids = "-1";
				if (minPre.equals(minPrefix)) {
					if (min.compareTo(tempMin) > 0) {
						min = tempMin;
					}
				} else if (pr.containsKey(minPrefix) && pr.containsKey(minPre)) {
					if (pr.get(minPrefix).intValue() == pr.get(minPre)
							.intValue()) {
						if (minPrefix.compareTo(minPre) > 0) {
							min = tempMin;
							minPrefix = minPre;
						}
					} else if (pr.get(minPrefix).intValue() < pr.get(minPre)
							.intValue()) {
						min = tempMin;
						minPrefix = minPre;
					}
				}

				context.getCounter(Counter.REMAININGURIS).increment(
						equivClasses.size());
				mos.write("sameAsP", new Text(min), new Text(sameAsURIs + "\t"
						+ ids + "\t" + finished), "sameAsP/");
				for (String u : alluris) {
					if (!u.equals(min))
						mos.write("sameAsP", new Text(u),
								new Text("Min:" + min), "sameAsP/");
				}
			}

		}

	}

	public static class SameAs extends Reducer<Text, Text, Text, Text> {
		private MultipleOutputs<Text, Text> mos;
		HashMap<String, Integer> pr = new HashMap<String, Integer>();
		int ID = 0;
		String code = "";
		TreeMap<Integer, HashSet<String>> sameAsList = new TreeMap<Integer, HashSet<String>>();
		HashMap<Integer, TreeSet<Integer>> urisID = new HashMap<Integer, TreeSet<Integer>>();
		HashMap<String, Integer> sameAsIndex = new HashMap<String, Integer>();

		@Override
		protected void setup(Context context) throws IOException {
			Configuration conf = context.getConfiguration();
			mos = new MultipleOutputs<Text, Text>(context);
			int id = context.getTaskAttemptID().getTaskID().getId();
			String finalID = Integer.toString(id);
			if (id < 10) {
				finalID = "0" + Integer.toString(id);
			}
			if (conf.get("job") != null)
				code = conf.get("job") + finalID;
			else
				code = "00" + finalID;

		}

		public void cleanup(Context context) throws IOException,
				InterruptedException {
			for (java.util.Map.Entry<Integer, HashSet<String>> entry : sameAsList
					.entrySet()) {
				String finished = "", datasets = "";
				int id = entry.getKey();
				for (String str : entry.getValue()) {
					finished += str + ",";
					mos.write("sameAsCatalog", str.replace("comma", ",")
							.replace("papaki", "@"), id + code,
							"sameAsCatalog/");
				}
				finished = finished.substring(0, finished.length() - 1);

				for (int i : urisID.get(entry.getKey())) {
					if (i != -1)
						datasets += i + ",";
				}
				if (datasets.length() > 1)
					datasets = datasets.substring(0, datasets.length() - 1);
				mos.write("sameAsChain",
						id + code + "\t" + finished.replace("papaki", "@"),
						new Text(datasets), "sameAsChain/");

				context.getCounter(Counter.CLASSESOFEQUIVALENCE).increment(1);
			}

			mos.close();
		}

		@Override
		public void reduce(Text key, Iterable<Text> values, Context context)
				throws IOException, InterruptedException {
			Set<Integer> datasets = new TreeSet<Integer>();

			for (Text val : values) {
				if (val.toString().startsWith("Min")
						|| val.toString().startsWith("Remove"))
					continue;
				Set<String> URIs = new HashSet<String>();
				String[] split = val.toString().split("\t");
				String[] uris = split[0].split(",");
				for (String u : uris) {
					String[] splitDegree = u.split("@");
					URIs.add(splitDegree[0]);
				}
				if (split.length == 3) {
					String[] fin = split[2].split(",");
					for (String u : fin) {
						URIs.add(u);
					}
				}
				String[] u = URIs.toArray(new String[URIs.size()]);
				String member1 = u[0];
				String[] ids = split[1].split(",");
				for (String x : ids) {
					if (StringUtils.isNumeric(x) && !x.equals("")
							&& !x.equals("-1"))
						datasets.add(Integer.parseInt(x));
				}
				for (int i = 1; i < u.length; i++) {
					String member2 = u[i];
					boolean member1InIndex = sameAsIndex.containsKey(member1);
					boolean member2InIndex = sameAsIndex.containsKey(member2);
					if (!member1InIndex && !member2InIndex) {
						sameAsIndex.put(member1, ID);
						sameAsIndex.put(member2, ID);
						HashSet<String> mem = new HashSet<String>();
						mem.add(member1);
						mem.add(member2);
						urisID.put(ID, new TreeSet<Integer>());
						urisID.get(ID).addAll(datasets);
						sameAsList.put(ID, mem);
						ID++;
					} else if (member1InIndex && !member2InIndex) {
						sameAsIndex.put(member2, sameAsIndex.get(member1));
						sameAsList.get(sameAsIndex.get(member1)).add(member2);
						urisID.get(sameAsIndex.get(member1)).addAll(datasets);
					} else if (!member1InIndex && member2InIndex) {
						sameAsIndex.put(member1, sameAsIndex.get(member2));
						sameAsList.get(sameAsIndex.get(member2)).add(member1);
						urisID.get(sameAsIndex.get(member2)).addAll(datasets);
					} else if (sameAsIndex.get(member2).intValue() != sameAsIndex
							.get(member1).intValue()) {
						int member1ID = sameAsIndex.get(member1);
						int member2ID = sameAsIndex.get(member2);
						int min, max;
						if (member1ID < member2ID) {
							min = new Integer(member1ID);
							max = new Integer(member2ID);
						} else {
							min = new Integer(member2ID);
							max = new Integer(member1ID);
						}
						for (String str : sameAsList.get(max)) {
							sameAsIndex.put(str, min);
						}
						ArrayList<String> list = new ArrayList<String>(
								sameAsList.get(max));
						sameAsList.get(min).addAll(list);
						urisID.get(min).addAll(urisID.get(max));
						sameAsList.remove(max);
						urisID.remove(max);
					}
				}

			}

		}

	}
}
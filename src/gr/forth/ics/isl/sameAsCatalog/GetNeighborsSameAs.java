package gr.forth.ics.isl.sameAsCatalog;

import gr.forth.ics.isl.preliminary.Prefix;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class GetNeighborsSameAs extends Configured implements Tool {

	public static void main(String[] args) throws Exception {

		
		Configuration configuration = new Configuration();

		configuration.setBoolean("mapred.compress.map.output", true);

		int res = ToolRunner.run(configuration, new GetNeighborsSameAs(), args);

		System.exit(res);
	}

	public static enum Counter {
		URISinONESOURCE, URISNUM, URISPOLICY1
	}

	@Override
	public int run(String[] args) throws Exception {
	
		String	jobid = "0" ;
		getConf().set("job", jobid);
		Job job = new Job(getConf(), "InvertedIndexApproach");
		job.setJarByClass(GetNeighborsSameAs.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		job.setMapperClass(Map.class);
		job.setReducerClass(Reduce.class);
		job.setNumReduceTasks(Integer.parseInt(args[2]));
		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		String[] m = { "sameAsP","finished" };
		for (String x : m) {
			MultipleOutputs.addNamedOutput(job, x, TextOutputFormat.class,
					Text.class, Text.class);

		}
		job.waitForCompletion(true);

		return 0;
	}

	public static class Map extends Mapper<LongWritable, Text, Text, Text> {
		HashMap<String, Prefix> pr = new HashMap<String, Prefix>();

		@Override
		public void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			FileSplit fsplit = (FileSplit) context.getInputSplit();
			String split[] = value.toString().split("\t");
			String datasetID ="-1";
			if (split.length == 2) {

				if ((value.toString().contains("purl.org")
						|| value.toString().contains("www.w3.org") || value
						.toString().contains("xmlns.com/foaf"))) {
					return;
				}
				if (!split[0].trim().equals(split[1].trim())) {

					context.write(new Text(split[0].trim()), new Text(datasetID
							+ "\t" + split[1].trim()));

					context.write(new Text(split[1].trim()), new Text(datasetID
							+ "\t" + split[0].trim()));
				}
			}
			if (split.length==1){
				context.write(new Text(split[0].trim()), new Text(""));
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

	public static class Reduce extends Reducer<Text, Text, Text, Text> {
		private MultipleOutputs<Text, Text> mos;
		String code = "";
		int plus=0;
		@Override
		protected void setup(Context context) throws IOException {
			Configuration conf = context.getConfiguration();
			mos = new MultipleOutputs<Text, Text>(context);
			int id = context.getTaskAttemptID().getTaskID().getId();
			String finalID = Integer.toString(id);
			if (id < 10) {
				finalID = "00" + Integer.toString(id);
			}
				code = "EIDD" + finalID;

		}
		public void cleanup(Context context) throws IOException,
				InterruptedException {
			mos.close();
		}

		@Override
		public void reduce(Text key, Iterable<Text> values, Context context)
				throws IOException, InterruptedException {
			Set<Integer> docs = new TreeSet<Integer>();
			Set<String> sameAs = new HashSet<String>();
			for (Text val : values) {
				String[] split = val.toString().split("\t");
				if (!split[0].equals("")) {
					docs.add(Integer.parseInt(split[0]));
				}

				if (split.length > 1) {
					sameAs.add(split[1].replace(",", "comma"));
				}
			}
			String datasetIDs = "";
			for (int z : docs)
				datasetIDs += z + ",";
			if (datasetIDs.length() > 0)
				datasetIDs = datasetIDs.substring(0, datasetIDs.length() - 1);
			if (!sameAs.isEmpty()) {
				Text input = new Text();
				input.set(key.toString());
				String sameAsPairs = "";
				for (String same : sameAs) {
					sameAsPairs += same + ",";
				}
				if (sameAsPairs.length() > 0)
					sameAsPairs = sameAsPairs.substring(0,
							sameAsPairs.length() - 1);
				String keyR = key.toString().replace(",", "comma");
				mos.write("sameAsP", new Text(keyR), new Text(sameAsPairs), "sameAsP/sas");

			}
			else{
				mos.write("finished", new Text(key), new Text(code+plus++), "finished/");
			}

		}

	}
}

package gr.forth.ics.isl.preliminary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

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

public class GetURIsofSource extends Configured implements Tool {

	public static void main(String[] args) throws Exception {

		Configuration configuration = new Configuration();

		configuration.setBoolean("mapred.compress.map.output", true);

		int res = ToolRunner.run(configuration, new GetURIsofSource(), args);

		System.exit(res);
	}

	public static enum Counter {
		URIS, TRIPLES
	}

	@Override
	public int run(String[] args) throws Exception {
		Job job = new Job(getConf(), "GetURISoFSource");
		job.setJarByClass(GetURIsofSource.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(NullWritable.class);
		job.setMapperClass(Map.class);
		job.setReducerClass(Reduce.class);
		job.setCombinerClass(Reduce.class);
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

	public static class Map extends
			Mapper<LongWritable, Text, Text, NullWritable> {
		HashMap<String, Prefix> pr = new HashMap<String, Prefix>();

		@Override
		public void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			FileSplit fsplit = (FileSplit) context.getInputSplit();
			String filename = fsplit.getPath().getName();
			String split2[] = filename.split("_");
			String split[] = value.toString().split("\t");
			String docID = "";
			if (split.length == 1 && split[0].trim().equals("") == false) {
				context.write(new Text(split[0].trim()), NullWritable.get());
			} else if (split.length >= 3 && !split[1].contains("differentFrom")) {
				context.getCounter(Counter.TRIPLES).increment(1);
				String subject = split[0].trim();
				String object = split[2].trim();
				if (subject.startsWith("http") || subject.startsWith("urn")
						|| subject.startsWith("uri")) {
					context.write(new Text(subject), NullWritable.get());
				}
				if (object.startsWith("http") || object.startsWith("urn")
						|| object.startsWith("uri")) {
					context.write(new Text(object), NullWritable.get());
				}
			}

		}

		@Override
		protected void setup(Context context) throws IOException {
			Configuration conf = context.getConfiguration();
		}

	}

	public static class Reduce extends
			Reducer<Text, NullWritable, Text, NullWritable> {
		private MultipleOutputs<Text, Text> mos;

		@Override
		protected void setup(Context context) {
			Configuration conf = context.getConfiguration();
			// mos=new MultipleOutputs<Text,Text>(context);
			// threshold=Double.parseDouble(conf.get("threshold"));
		}

		public void cleanup(Context context) throws IOException,
				InterruptedException {
			// mos.close();
		}

		@Override
		public void reduce(Text key, Iterable<NullWritable> values,
				Context context) throws IOException, InterruptedException {

			context.write(key, NullWritable.get());
			context.getCounter(Counter.URIS).increment(1);

		}

	}

}

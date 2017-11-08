/*
This code belongs to the Semantic Access and Retrieval (SAR) group of the 
Information Systems Laboratory (ISL) of the 
Institute of Computer Science (ICS) of the  
Foundation for Research and Technology – Hellas (FORTH)

Nobody is allowed to use, copy, distribute, or modify this work.
It is published for reasons of research results reproducibility.

© 2017, Semantic Access and Retrieval group, All rights reserved
 
 */
package gr.forth.ics.isl.latticeCreation;

import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class CreateDirectCounts extends Configured implements Tool {

	public static void main(String[] args) throws Exception {

		Configuration configuration = new Configuration();

		configuration.setBoolean("mapred.compress.map.output", true);

		int res = ToolRunner.run(configuration, new CreateDirectCounts(), args);

		System.exit(res);
	}

	@Override
	public int run(String[] args) throws Exception {
		Job job = new Job(getConf(), "DirectCounts");
		job.setJarByClass(CreateDirectCounts.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
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

	public static class Map extends
			Mapper<LongWritable, Text, Text, IntWritable> {
		@Override
		public void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			String line = value.toString();
			if (line.trim().equals("")) {
				return;
			}
			String[] x = line.split("\t");
			if ((value.toString().contains("purl.org")
					|| value.toString().contains("www.w3.org") || value
					.toString().contains("xmlns.com/foaf"))) {
				return;
			}
			if (x.length > 1)
				context.write(new Text(x[1]), new IntWritable(1));
		}
	}

	public static class Reduce extends
			Reducer<Text, IntWritable, Text, IntWritable> {

		@Override
		protected void setup(Context context) {
			Configuration conf = context.getConfiguration();
		}

		@Override
		public void reduce(Text key, Iterable<IntWritable> values,
				Context context) throws IOException, InterruptedException {
			int value = 0;
			for (IntWritable val : values) {
				value += val.get();
			}
			context.write(key, new IntWritable(value));

		}

	}

}

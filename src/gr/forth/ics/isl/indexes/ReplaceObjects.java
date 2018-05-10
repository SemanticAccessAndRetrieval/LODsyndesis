package gr.forth.ics.isl.indexes;

import gr.forth.ics.isl.latticeCreation.CreateLattice;
import gr.forth.ics.isl.preliminary.Prefix;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper.Context;
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


public class ReplaceObjects extends Configured implements Tool {

	public static void main(String[] args) throws Exception {

		Configuration configuration = new Configuration();

		configuration.setBoolean("mapred.compress.map.output", true);

		int res = ToolRunner.run(configuration, new ReplaceObjects(),
				args);

		System.exit(res);
	}

	public static enum Counter {
		URISinONESOURCE, URISNUM, URISPOLICY1
	}

	@Override
	public int run(String[] args) throws Exception {
		getConf().set("dfs.replication", "1");
		Job job2 = new Job(getConf(), "elementIndexPart2");
		job2.setJarByClass(CreateLattice.class);
		job2.setOutputKeyClass(Text.class);
		job2.setOutputValueClass(Text.class);
		job2.setMapperClass(Map2.class);
		job2.setReducerClass(Reduce2.class);
        job2.setNumReduceTasks(Integer.parseInt(args[2]));
       // job2.addCacheFile(new Path(args[3]).toUri());
        job2.setInputFormatClass(TextInputFormat.class);
        job2.setOutputFormatClass(TextOutputFormat.class);
        FileInputFormat.addInputPath(job2, new Path(args[0]));
        FileOutputFormat.setOutputPath(job2, new Path(args[1]));
       job2.waitForCompletion(true);
		return 0;
	}

	
	
	
	
	public static class Map2 extends Mapper<LongWritable, Text, Text, Text> {
		//HashMap<String, String> pr = new HashMap<String, String>();

		@Override
		public void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			
			//String split2[] = filename.split("_");
			String split[] = value.toString().split("\t");
			
			if (split.length ==2) {
				context.write(new Text(split[0].trim()),
						new Text(split[1].trim()));
			}
			if (split.length >=3) {
				String cl="";
				//if(pr.containsKey(split[2].trim())){
					//cl=pr.get(split[2].trim());
					//context.write(new Text(cl),new Text(split[0].trim()+"\t"+split[1].trim()+"\t"+split[3].trim()));
					
			//	}
				//else{
					context.write(new Text(split[2].trim()),
							new Text(split[0].trim()+"\t"+split[1].trim()+"\t"+split[3].trim()));
			//	}
				}
			
		} 
		
		
		protected void setup(Context context) throws IOException {
			Configuration conf = context.getConfiguration();
			java.net.URI[] LocalPaths = null;
			
		}}

		
	
	
	public static class Reduce2 extends Reducer<Text, Text, Text, Text> {
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
			
			Set<String> docs = new HashSet<String>();
			String SID="";
			for (Text val : values) {
			
				if(!val.toString().contains("\t") && val.toString().startsWith("EID")){
					SID=val.toString();
				}
				else if(!SID.equals("")){
				//System.out.println(val.toString());
					
						String [] split=val.toString().split("\t");
						if (split.length>=3)
						context.write(new Text(split[0]+"\t"+split[1]), new Text(SID+"\t"+split[2]));

					
				
				}
				else{
					//System.out.println(val.toString());
					docs.add(new String(val.toString()));
				}
			}
			
			for(String val:docs){
				String [] split=val.toString().split("\t");
			
				if (split.length>=3)
					context.write(new Text(split[0]+"\t"+split[1]), new Text(SID+"\t"+split[2]));
			}
			
			

			}

	}
	

}

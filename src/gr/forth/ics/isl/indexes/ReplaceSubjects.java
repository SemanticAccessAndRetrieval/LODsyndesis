package gr.forth.ics.isl.indexes;

import gr.forth.ics.isl.indexes.CreateEntityTriplesIndex.Counter;
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


public class ReplaceSubjects extends Configured implements Tool {

	public static void main(String[] args) throws Exception {

		Configuration configuration = new Configuration();

		configuration.setBoolean("mapred.compress.map.output", true);

		int res = ToolRunner.run(configuration, new ReplaceSubjects(),
				args);

		System.exit(res);
	}

	public static enum Counter {
		INPUT, Noblank
	}

	@Override
	public int run(String[] args) throws Exception {
		getConf().set("dfs.replication", "1");
		Job job = new Job(getConf(), "ElementIndexPart1");
		job.setJarByClass(CreateElementIndex.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		job.setMapperClass(Map.class);
		job.setReducerClass(Reduce.class);

		job.setNumReduceTasks(Integer.parseInt(args[2]));

		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		job.addCacheFile(new Path(args[3]).toUri());
		job.addCacheFile(new Path(args[4]).toUri());
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		String[] m = { "finished", "object" };
		for (String x : m) {
			MultipleOutputs.addNamedOutput(job, x, TextOutputFormat.class,
					Text.class, Text.class);

		}
		job.waitForCompletion(true);
	
		return 0;
	}

	public static class Map extends Mapper<LongWritable, Text, Text, Text> {
		HashMap<String, String> pr = new HashMap<String, String>();
		HashMap<String, String> cl = new HashMap<String, String>();
		@Override
		public void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			FileSplit fsplit = (FileSplit) context.getInputSplit();
			String filename = fsplit.getPath().getName();
			String split2[] = filename.split("_");
			String split[] = value.toString().split("\t");
			String datasetID = "";
			
			if (split.length ==2) {
				context.write(new Text(split[0].trim()),
						new Text(split[1].trim()));
			}
			if (split.length >=3 ) {
				context.getCounter(Counter.INPUT).increment(1);
				datasetID = split2[0];
				if(!split[0].startsWith("_:") && !split[2].startsWith("_:")){
					context.getCounter(Counter.Noblank).increment(1);

					String property="";
					String object=split[2].trim().replace("\"", "");
					if(pr.containsKey(split[1].trim()))
						property=pr.get(split[1].trim());
					if (object.startsWith("http://") || object.startsWith("urn")){
						if(cl.containsKey(object))
							object=cl.get(object);
					}
					else{
						object=object.toLowerCase();
					}
					context.write(new Text(split[0].trim()),
							new Text(property+"\t"+object+"\t"+datasetID));
				}} }
		


	
	
	
	

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

					pr.put(split[0], split[1]);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			br.close();
			
			
			FSDataInputStream out1 = fs.open(new Path(LocalPaths[1].getPath()));
			String line1;
			BufferedReader br1 = null;

			br1 = new BufferedReader(new InputStreamReader(out1));
			try {
				while ((line1 = br1.readLine()) != null) {
					String split[] = line1.split("\t");

					cl.put(split[0], split[1]);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			br1.close();

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
			Set<String> docs = new HashSet<String>();
			String SID="";
			for (Text val : values) {
				
				if(val.toString().startsWith("EID")){
					SID=val.toString();//
				}
				else if(!SID.equals("")){
					if (val.toString().split("\t").length<2)
						continue;
				//System.out.println(val.toString());
					if(val.toString().split("\t")[1].startsWith("http://") || val.toString().split("\t")[1].startsWith("urn")){
						mos.write("object", SID, val.toString(), "object/");
					}
					else
						mos.write("finished", SID, val.toString(), "finished/");
				
				}
				else{
				
					docs.add(new String(val.toString()));
				}
			}
			
			for(String val:docs){
				if (val.toString().split("\t").length<2)
					continue;
				if(val.toString().split("\t")[1].startsWith("http://") || val.toString().split("\t")[1].startsWith("urn")){
					mos.write("object", SID, val.toString(), "object/");
				}
				else
					mos.write("finished", SID, val.toString(), "finished/");
			}
			
		}

	}
	
	
}

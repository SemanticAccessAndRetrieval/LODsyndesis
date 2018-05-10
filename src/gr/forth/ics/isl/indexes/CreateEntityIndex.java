package gr.forth.ics.isl.indexes;



import gr.forth.ics.isl.indexes.CreateCommonLiteralsIndex.Counter;

import java.io.IOException;

	import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

	import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
	
	import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
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


	public class CreateEntityIndex  extends Configured implements Tool {

		public static void main(String[] args) throws Exception {

			Configuration configuration = new Configuration();

			configuration.setBoolean("mapred.compress.map.output", true);

			int res = ToolRunner.run(configuration, new CreateEntityIndex(),
					args);

			System.exit(res);
		}

		public static enum Counter {
			Literals
		}

		@Override
		public int run(String[] args) throws Exception {
			getConf().set("dfs.replication", "1");
			int type = Integer.parseInt(args[3]);
			getConf().set("type", type + "");
			Job job = new Job(getConf(), "literalsIndex");
			job.setJarByClass(CreateEntityIndex.class);
			
			job.setOutputKeyClass(Text.class);
			job.setOutputValueClass(Text.class);
			job.setMapperClass(Map.class);
			job.setReducerClass(Reduce.class);

			job.setNumReduceTasks(Integer.parseInt(args[2]));

			job.setInputFormatClass(TextInputFormat.class);
			job.setOutputFormatClass(TextOutputFormat.class);
			FileInputFormat.addInputPath(job, new Path(args[0]));
			FileOutputFormat.setOutputPath(job, new Path(args[1]));
			String[] m = { "entities", "properties","classes" };
			for (String x : m) {
				MultipleOutputs.addNamedOutput(job, x, TextOutputFormat.class,
						Text.class, Text.class);

			}
			job.waitForCompletion(true);
		
			return 0;
		}

		public static class Map extends Mapper<LongWritable, Text, Text, Text> {
			HashMap<String, String> pr = new HashMap<String, String>();
			//int object;
			@Override
			public void map(LongWritable key, Text value, Context context)
					throws IOException, InterruptedException {
				FileSplit fsplit = (FileSplit) context.getInputSplit();
				String filename = fsplit.getPath().getName();
				String split2[] = filename.split("_");
				HashSet<String> prev=new HashSet<String>();
				String split[] = value.toString().split("\t");
				String datasetID = "";
				
				if (split.length >=4){
					datasetID = split[3];
					if(split[0].startsWith("EID") && !prev.contains(split[0])){
						context.write(new Text(split[0]), new Text(datasetID));
						prev.add(split[0]);
					}
					if(split[2].startsWith("EID") && !prev.contains(split[2])){
						context.write(new Text(split[2]), new Text(datasetID));
						prev.add(split[2]);
					}
					
					if(split[2].startsWith("C") && !prev.contains(split[2])){
						context.write(new Text(split[2]), new Text(datasetID));
						prev.add(split[2]);
					}
			
					if(split[1].startsWith("P") && !prev.contains(split[1])){
						context.write(new Text(split[1]), new Text(datasetID));
						prev.add(split[1]);
					}
					if(prev.size()==50000)
						prev.clear();
			}
			}
			
			protected void setup(Context context) {
				Configuration conf = context.getConfiguration();
				
				//object = Integer.parseInt(conf.get("object"));
				//mos = new MultipleOutputs<Text, Text>(context);
				// threshold=Double.parseDouble(conf.get("threshold"));
			}

		
		
		
		}

		
			
		public static class Reduce extends Reducer<Text, Text, Text, Text> {
			private MultipleOutputs<Text, Text> mos;
			int type=0;
			@Override
			protected void setup(Context context) {
				Configuration conf = context.getConfiguration();
				type = Integer.parseInt(conf.get("type"));
				//object = Integer.parseInt(conf.get("object"));
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
				HashMap<String, Set<Integer>> index = new HashMap<String, Set<Integer>>();
				for (Text val : values) {
					String lit = key.toString();
					if (!index.containsKey(lit)) {
						Set<Integer> datasets = new TreeSet<Integer>();
						datasets.add(Integer.parseInt(val.toString()));
						index.put(lit, datasets);
					} else {
						index.get(lit).add(Integer.parseInt(val.toString()));
					}

				}

				for (java.util.Map.Entry<String, Set<Integer>> entry : index
						.entrySet()) {
					Set<Integer> datasets = entry.getValue();
					String ids = "";
					if (datasets.size() > 0) {
						for (int x : datasets) {
							ids += x + ",";
						}
						ids = ids.substring(0, ids.length() - 1);
						if(datasets.size()==1 && type==1)
							continue;
						if(entry.getKey().startsWith("EID"))
							mos.write("entities", entry.getKey(), ids, "entities/");
						if(entry.getKey().startsWith("C"))
							mos.write("classes", entry.getKey(), ids, "classes/");
						if(entry.getKey().startsWith("P"))
							mos.write("properties", entry.getKey(), ids, "properties/");
						
					}

				}

				
		}
		}
		
		
	}


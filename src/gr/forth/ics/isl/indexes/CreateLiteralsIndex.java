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


	public class CreateLiteralsIndex  extends Configured implements Tool {

		public static void main(String[] args) throws Exception {

			Configuration configuration = new Configuration();

			configuration.setBoolean("mapred.compress.map.output", true);

			int res = ToolRunner.run(configuration, new CreateLiteralsIndex(),
					args);

			System.exit(res);
		}

		public static enum Counter {
			Literals,LiteralsAll, LiteralsTWO, LiteralsTHREE
		}

		@Override
		public int run(String[] args) throws Exception {
			getConf().set("dfs.replication", "1");
			int type = Integer.parseInt(args[3]);
			getConf().set("type", type + "");
			Job job = new Job(getConf(), "literalsIndex");
			job.setJarByClass(CreateLiteralsIndex.class);
			job.setOutputKeyClass(Text.class);
			job.setOutputValueClass(Text.class);
			job.setMapperClass(Map.class);
			job.setReducerClass(Reduce.class);

			job.setNumReduceTasks(Integer.parseInt(args[2]));

			job.setInputFormatClass(TextInputFormat.class);
			job.setOutputFormatClass(TextOutputFormat.class);
			FileInputFormat.addInputPath(job, new Path(args[0]));
			FileOutputFormat.setOutputPath(job, new Path(args[1]));
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
				String split[] = value.toString().split("\t");
				String datasetID = "";
				if (split.length >=4 && !split[2].startsWith("E") 
						&& !split[2].startsWith("C")) {
					datasetID = split[3];
				
				String [] split3= split[2].trim().split("@");
				String object="";	
				if(split3.length>=1){
					String[] split4=split3[0].replace("^^","\t").split("\t");
					if(split4.length>=1){
						object=split4[0].trim().replace("\"", "");
					}
					else{
						object=split[2].trim().replace("\"", "");
					}
				}
				else{
					object=split[2].trim().replace("\"", "");
				}
				
				String key1;
					if (object.length() >= 10)
						key1 = object.substring(0, 9);
					else
						key1 = object;
					context.write(new Text(key1), new Text(object + "\t" + datasetID
							+ "\t"));
				

				
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
				//mos = new MultipleOutputs<Text, Text>(context);
				// threshold=Double.parseDouble(conf.get("threshold"));
			}

			public void cleanup(Context context) throws IOException,
					InterruptedException {
				//mos.close();
			}

			@Override
			public void reduce(Text key, Iterable<Text> values, Context context)
					throws IOException, InterruptedException {
				HashMap<String, Set<Integer>> litIndex = new HashMap<String, Set<Integer>>();
				for (Text val : values) {
					String[] split = val.toString().split("\t");
					String lit = split[0];
					if (!litIndex.containsKey(lit)) {
						Set<Integer> datasets = new TreeSet<Integer>();
						if(split[1].contains("r"))
							continue;
						datasets.add(Integer.parseInt(split[1]));
						litIndex.put(lit, datasets);
					} else {
						litIndex.get(lit).add(Integer.parseInt(split[1]));
					}

				}

				for (java.util.Map.Entry<String, Set<Integer>> entry : litIndex
						.entrySet()) {
					Set<Integer> datasets = entry.getValue();
					String ids = "";
					context.getCounter(Counter.LiteralsAll).increment(1);

					if (datasets.size() > 1) {
						
						for (int x : datasets) {
							ids += x + ",";
						}
						ids = ids.substring(0, ids.length() - 1);
						if(datasets.size()==1 && type==1)
							continue;
						context.write(new Text(entry.getKey()), new Text(ids));
						if (datasets.size() ==2 )
							context.getCounter(Counter.LiteralsTWO).increment(1);
						if (datasets.size() >=3 )
							context.getCounter(Counter.LiteralsTHREE).increment(1);
					}

				}

				
		}
		}
		
		
	}


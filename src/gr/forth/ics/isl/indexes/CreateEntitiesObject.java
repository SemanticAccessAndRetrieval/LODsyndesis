package gr.forth.ics.isl.indexes;



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


	public class CreateEntitiesObject  extends Configured implements Tool {

		public static void main(String[] args) throws Exception {

			Configuration configuration = new Configuration();

			configuration.setBoolean("mapred.compress.map.output", true);

			int res = ToolRunner.run(configuration, new CreateEntitiesObject(),
					args);

			System.exit(res);
		}

		public static enum Counter {
			TWODATASETS,ONEDATASETS,FOURDATASETS,THREEDATASETS
		}

		@Override
		public int run(String[] args) throws Exception {
			getConf().set("dfs.replication", "1");
			int type = Integer.parseInt(args[3]);
			getConf().set("type", type + "");
			Job job = new Job(getConf(), "triples");
			job.setJarByClass(CreateEntitiesObject.class);
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

			@Override
			public void map(LongWritable key, Text value, Context context)
					throws IOException, InterruptedException {
				FileSplit fsplit = (FileSplit) context.getInputSplit();
				String filename = fsplit.getPath().getName();
				String split[] = value.toString().split("\t");
				String datasetID = "";
				if (split.length >=4) {
					datasetID = split[3];
					//if(split[0].startsWith("E")){
				String [] split3= split[2].trim().split("@");
				if(split3.length>=1){
					String[] split4=split3[0].replace("^^","\t").split("\t");
					if(split4.length>=1){

						context.write(new Text(split[0].trim()),
								new Text(split4[0].trim().replace("\"", "")+"\t"+datasetID));
					}
					else{
						context.write(new Text(split[0].trim()),
								new Text(split[2].trim().replace("\"", "")+"\t"+datasetID));
					}
				}
				else{
					context.write(new Text(split[0].trim()),
							new Text(split[2].trim().replace("\"", "")+"\t"+datasetID));
				}
				
						
				//	}
				//	if(split[2].startsWith("EID") && !split[2].contains(" ")){
				//		context.write(new Text(split[2].trim()),
				//				new Text(split[1].trim()+"*"+"\t"+split[0].trim().replace("\"", "")+"\t"+datasetID));
				//	}
				
			}
			}
			


		
		
		
		}

		
			
		public static class Reduce extends Reducer<Text, Text, Text, Text> {
			private MultipleOutputs<Text, Text> mos;
			int type=0;
			@Override
			protected void setup(Context context) {
				Configuration conf = context.getConfiguration();
				type = Integer.parseInt(conf.get("type"));
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
				Set<String> docs = new HashSet<String>();
				HashMap<String,TreeSet<Integer>> index=new HashMap<>() ;
				for (Text val : values) {
					String split[] = val.toString().split("\t");
					String object=split[0];
					if(split[1].contains("r"))
						continue;
					int id=Integer.parseInt(split[1]);
					
						if(index.containsKey(object)){
							index.get(object).add(id);
						}
						else{
							TreeSet<Integer> mk=new TreeSet<>();
							mk.add(id);
							index.put(object, mk);
						}
					
					
				}
			
				//output+=key+"\n";
				//context.write(new Text(key), new Text());
					for(String p:index.keySet()){
						//context.write(new Text("\t"), new Text(p));
						String ids="";
						boolean write=true;
						if (index.get(p).size()==2){
							context.getCounter(Counter.TWODATASETS).increment(1);
						}
						else  if (index.get(p).size()==3){
							context.getCounter(Counter.THREEDATASETS).increment(1);
						}
						else  if (index.get(p).size()>=4){
							context.getCounter(Counter.FOURDATASETS).increment(1);
						}
						else{
							context.getCounter(Counter.ONEDATASETS).increment(1);
							if(type==1)
							write=false;
						}
						for(int id:index.get(p)){
							ids+=id+",";
						}
						ids=ids.substring(0,ids.length()-1);
						//output+="\t\t"+p+"\t"+ids+"\n";
						if(write==true)
						context.write(new Text(key), new Text(p+"\t"+ids));
					}
				
				//context.write(new Text(output),NullWritable.get());
				
				
			}

		}
		
		
	}


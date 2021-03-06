package gr.forth.ics.isl.preliminary;


import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
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



public class GetLiteralsOfSource extends Configured implements Tool {
	   
		public static void main(String[] args) throws Exception {
	      
	      Configuration configuration= new Configuration();
	      
	      configuration.setBoolean("mapred.compress.map.output",true);
	      
	      
	      int res = ToolRunner.run(configuration, new GetLiteralsOfSource(), args);
	      
	      System.exit(res);
	   }
	   
	   public static enum Counter{
		   URIS,TRIPLES
	   }
	   
	   

	   @Override
	   public int run(String[] args) throws Exception {
	      Job job = new Job(getConf(), "LiteralsOfSource");
	      job.setJarByClass(GetLiteralsOfSource.class);
	      job.setOutputKeyClass(Text.class);
	      job.setOutputValueClass(Text.class);
	      job.setMapperClass(Map.class);
	      job.setReducerClass(Reduce.class);
	     // job.setCombinerClass(Reduce.class);
	      job.setNumReduceTasks(Integer.parseInt(args[2]));
	      job.setInputFormatClass(TextInputFormat.class);
	      job.setOutputFormatClass(TextOutputFormat.class);
	      //job.setPartitionerClass(myPartitioner.class);
	      FileInputFormat.addInputPath(job, new Path(args[0]));
	      FileOutputFormat.setOutputPath(job, new Path(args[1]));
	      //job.addCacheFile(new Path(args[2]).toUri());
	      String[] m={"D0","D1","D2"};
	      for (int x=0;x<=304;x++){
	        MultipleOutputs.addNamedOutput(job, "D"+x,TextOutputFormat.class, Text.class, Text.class);
	      
	      }
	      job.waitForCompletion(true);
	      
	      return 0;
	   }
	   
	   public void writeHDFSFile(String message,FileSystem fs){
		   Path filePath=new Path("comparisons_number_inverted_index_approach.txt");
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
	      HashMap<String,Prefix> pr=new HashMap<String,Prefix>();
	      @Override
	      public void map(LongWritable key, Text value, Context context)
	              throws IOException, InterruptedException {
	    	 FileSplit fsplit=(FileSplit) context.getInputSplit();
	    	 String filename=fsplit.getPath().getName();;
	    	 String split[]= value.toString().split("\t");
	    	 String[] x=split[1].split(",");
	    	 if (split.length>=2){
	    		 context.write(new Text(split[0]),new Text(split[1]));
	    	 }
	    	
	        		
	     }
	      
	      @Override
	      protected void setup(Context context) throws IOException{
	    	  Configuration conf=context.getConfiguration();	
	      }
	   
	      
	}
	 

		   
	   

	   
	   public static class Reduce extends Reducer<Text, Text, Text, NullWritable> {
		   private MultipleOutputs<Text,NullWritable> mos;
		   
		   @Override
		      protected void setup(Context context){
		    	  Configuration conf=context.getConfiguration();
		    	  mos=new MultipleOutputs<Text,NullWritable>(context);
		    	//  threshold=Double.parseDouble(conf.get("threshold"));
		      }
		   
		   public void cleanup(Context context) throws IOException, InterruptedException{
			   mos.close();
		   }
	      @Override
	      public void reduce(Text key, Iterable<Text> values, Context context)
	              throws IOException, InterruptedException {
	    	  String sid="-1";
	    	  Set<Integer> sources=new HashSet<Integer>();
	    	  for (Text val: values){
	    			  String[] split=val.toString().split(",");
	    			  for (String x:split){
	    				  sources.add(Integer.parseInt(x));
	    			 
	    	  }
	    	  for(int x:sources){
	    		  if(x>=0 && x<=304)
	    				  mos.write("D"+x,key, NullWritable.get(),"D"+x+"/");
	    	  }
	    	  
	    	 
	      }
	      

	   }
	   }

}
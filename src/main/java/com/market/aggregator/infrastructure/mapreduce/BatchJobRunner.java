package com.market.aggregator.infrastructure.mapreduce;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class BatchJobRunner {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: BatchJobRunner <input path> <output path>");
            System.exit(-1);
        }
        Configuration conf = new Configuration();
        conf.set("batch.record.lines", "10");
        Job job = Job.getInstance(conf, "Market Aggregator Batch Processing");
        job.setJarByClass(BatchJobRunner.class);

        job.setInputFormatClass(BatchLineInputFormat.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        job.setMapperClass(BatchMapper.class);
        job.setReducerClass(BatchReducer.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
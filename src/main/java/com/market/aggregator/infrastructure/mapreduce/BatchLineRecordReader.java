package com.market.aggregator.infrastructure.mapreduce;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.input.LineRecordReader;
import org.apache.hadoop.mapreduce.RecordReader;

public class BatchLineRecordReader extends RecordReader<LongWritable, Text> {
    private final LineRecordReader lineReader = new LineRecordReader();
    private LongWritable key = new LongWritable();
    private Text value = new Text();
    private final List<String> batch = new ArrayList<>();
    private int batchSize = 1000; // adjust if needed

    @Override
    public void initialize(org.apache.hadoop.mapreduce.InputSplit split, TaskAttemptContext context) throws IOException, InterruptedException {
        lineReader.initialize(split, context);
        String batchSizeProp = context.getConfiguration().get("batch.record.lines");
        if (batchSizeProp != null) {
            batchSize = Integer.parseInt(batchSizeProp);
        }
    }

    @Override
    public boolean nextKeyValue() throws IOException, InterruptedException {
        batch.clear();
        int count = 0;
        while (count < batchSize && lineReader.nextKeyValue()) {
            batch.add(lineReader.getCurrentValue().toString());
            count++;
        }
        if (batch.isEmpty()) return false;
        key.set(lineReader.getCurrentKey().get());
        value.set(String.join("\n", batch));
        return true;
    }

    @Override
    public LongWritable getCurrentKey() {
        return key;
    }

    @Override
    public Text getCurrentValue() {
        return value;
    }

    @Override
    public float getProgress() throws IOException {
        return lineReader.getProgress();
    }

    @Override
    public void close() throws IOException {
        lineReader.close();
    }
}

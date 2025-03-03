package com.market.aggregator.infrastructure.mapreduce;

import java.io.IOException;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

public class BatchLineInputFormat extends TextInputFormat {
    @Override
    public RecordReader<LongWritable, Text> createRecordReader(InputSplit split, TaskAttemptContext context) {
        BatchLineRecordReader reader = new BatchLineRecordReader();
        try {
            reader.initialize(split, context);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return reader;
    }
}

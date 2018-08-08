package com.iswoqqe.lox.lib;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PersistentHashMapBenchmark {
    static private PersistentHashMap<Object, Object> phmap = new PersistentHashMap<>();
    static private Map<Object, Object> hmap = new HashMap<>();

    static final private int items = 1000000;

    static {
        for (int i = 0; i < items; ++i) {
            phmap = phmap.with(i, 'a');
            hmap.put(i, 'a');
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.SampleTime)
    public void measurePersistentHashMapInsertionTime() {
        PersistentHashMap<Object, Object> map = new PersistentHashMap<>();
        for (int i = 0; i < items; ++i) {
            map = map.with(1, 'a');
            map = map.with(i, 'b');
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.SampleTime)
    public void measureHashMapInsertionTime() {
        Map<Object, Object> map = new HashMap<>();
        for (int i = 0; i < items; ++i) {
            map.put(1, 'a');
            map.put(i, 'b');
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.SampleTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measurePersistentHashQueryTime() {
        for (int i = 0; i < items; ++i) {
            phmap.get(i);
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.SampleTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureHashQueryTime() {
        for (int i = 0; i < items; ++i) {
            hmap.get(i);
        }
    }
}

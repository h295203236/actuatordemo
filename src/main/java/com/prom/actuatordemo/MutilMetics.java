package com.prom.actuatordemo;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.MeterBinder;

import java.util.HashMap;
import java.util.Map;

/**
 * 多指标自定义
 */
public class MutilMetics implements MeterBinder {

    public Counter counter01;
    public Timer timer01;

    public Map<String, Double> valuesMap = new HashMap<>();

    @Override
    public void bindTo(MeterRegistry meterRegistry) {
        this.counter01 = Counter.builder("demo.counter01")
                .tags("app", "app1")
                .description("execute count").register(meterRegistry);

        this.timer01 = Timer.builder("demo.timer01")
                .tags("app", "app1")
                .description("function execute time").register(meterRegistry);

        Gauge.builder("demo.gauge01", valuesMap, vm -> vm.get("gauge01"))
                .tags("app", "app1")
                .description("gauge01 demo")
                .register(meterRegistry);

        Gauge.builder("demo.gauge02", valuesMap, vm -> vm.get("gauge02"))
                .tags("app", "app1")
                .description("gauge01 demo")
                .register(meterRegistry);
    }
}

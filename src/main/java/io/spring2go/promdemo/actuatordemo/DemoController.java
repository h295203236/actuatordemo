package io.spring2go.promdemo.actuatordemo;

import io.micrometer.core.instrument.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;

@RestController
public class DemoController {

    // 注册缓存中心：用于指标收集缓存
    @Autowired
    MeterRegistry meterRegistry;
    private Counter counter01;

    @PostConstruct
    void init() {
        this.counter01 = meterRegistry.counter("demo.counter01", Tags.of("tag1", "value1", "tag2", "value2"));
    }

    @RequestMapping(value = "/counter")
    public @ResponseBody
    String sayCounter() {
        // step设置为5（Double）
        //this.counter01.increment(5D);
        this.counter01.increment();
        return "hello";
    }

    @RequestMapping(value = "/timer")
    public @ResponseBody
    String sayTimer() {
        try {
            // 计时器
            Timer.Sample sample = Timer.start(this.meterRegistry);  // 开始计时
            Thread.sleep(1000); // 测试延时
            // 计时结束并汇报给注册缓存中心
            sample.stop(this.meterRegistry.timer("demo.timer01", Tags.of("tag1", "value1", "tag2", "value2")));
        } catch (InterruptedException ignored) {
        }
        return "hello";
    }

    @RequestMapping(value = "/gauge")
    public @ResponseBody
    String sayGauge() {
        // 瞬时指标
        double value = System.currentTimeMillis() / 1000.0;
        this.meterRegistry.gauge("demo.gauge01", Tags.of("tag1", "value1", "tag2", "value2"), value, Double::doubleValue);
        return "hello";
    }
}

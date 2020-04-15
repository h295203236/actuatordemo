## Spring boot自定义指标使用

springboot已经对Prometheus自定义指标有比较深入的集成，目前看已有的集成方式已经比较简单可以做到。

包引用：

**spring-boot-starter-actuator：** springboot自带的指标收集库，与micrometer已经深度集成；
**micrometer-registry-prometheus：** springboot与promtheus对接标准库；

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-actuator</artifactId>
 </dependency>
 <dependency>
  <groupId>io.micrometer</groupId>
  <artifactId>micrometer-registry-prometheus</artifactId>
 </dependency>
```

支持指标类型：

`Gauge：` 标准值，瞬时值，如cpu、内存等；

`Counter：` 计数器

`Timer：` 计时器

其他如分布式`Summary`指标、`FunctionCounter`、`LongTimeTask`等

Spring中配置

```yml
server.port: 8080
spring.application.name: app1
management:
  server.port: 10080
  endpoints:
    web.exposure.include: '*'
    base-path: /management
  # 全局通用tag标识application name
  metrics.tags.application: ${spring.application.name}
```

Spring中自定义指标使用：

```java
package io.spring2go.promdemo.actuatordemo;

import io.micrometer.core.instrument.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;

@RestController
public class DemoController {

    // 注册缓存中心：用于指标汇报收集缓存
    @Autowired
    MeterRegistry meterRegistry;
  
  	// 计数器
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
```

多个指标定义：没有那么灵活，比较固定

```java
@Component
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

@Component
public class Test {
  @Autowired
  MutilMetics multiMetrics;
  
  public void function1() {
    multiMetrics.counter01.increment();
    multiMetrics.valuesMap.put("gauge01", Double.valueof(11.0));
    multiMetrics.valuesMap.put("gauge02", Double.valueof(9.0));
    ...
  }
}
```


文档：

spring-boot：https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#production-ready-metrics-export-prometheus  

IBM：https://www.ibm.com/developerworks/cn/java/j-using-micrometer-to-record-java-metric/index.html  

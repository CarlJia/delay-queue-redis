## delay-queue-redis

基于redis缓存的延迟队列，内部依赖redis的数据结构，形如```hash zset```，通过lua保持多命令的事务性。  
内嵌在业务进程内执行， 目前支持原生的spring-data-redis和jimDB(均需要开启hashtag)。

## 原理
![概要流程图](https://github.com/glameyzhou/delay-queue-redis/raw/master/docs/delay-queue-redis.drawio.png)
## 注意事项

- 比较适合小量级的延迟场景，形如：业务异常重试。
- 目前设定的最小延迟时间是**1s**，最大**7天**。
- 延迟数据容灾完全依赖redis备份。
- 业务进程强杀，丢失最多**10条**数据(如果用户没有修改默认配置的话)。

## 概要
```bash
模块介绍
├── README.md
├── delay-queue-redis-core                        核心主流程
├── delay-queue-redis-data-spring-boot-starter    基于spring-data-redis实现的spring-boot-starter
├── delay-queue-redis-jimdb-spring-boot-starter   基于jimDB实现的spring-boot-starter
├── delay-queue-redis-spring-boot-starter-demo    样例:基于deley-queue-redis-data-spring-boot-starter实现
└── pom.xml
```
具体样例请参考module```delay-queue-redis-spring-boot-starter-demo```，共四个步骤：
```xml
        <!--暂时未发正式版本-->
        <dependency>
            <groupId>o2o.platform.commons</groupId>
            <artifactId>delay-queue-redis-jimdb-spring-boot-starter</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
```
- 引用jar包 delay-queue-redis-[jimdb|data]-spring-boot-starter.jar。
- 启动函数添加```@EnabledDelayQueue``` 开启delay-queue-redis的功能。
- 增加application.properties相关配置，形如：生产者、消费者、topic等，大部分保持默认即可。比较重要的是```delay.queue.topics```
  配置。
- 增加topics对应的消费者，记得把消费者的handler名称配置到```delay.queue.topics```即可。
- 可以自定义异常消费，内部有默认的可重入实现。

## 接入详情

### 1. 启动类添加注解，开启延迟队列

SpringApplication启动类头部添加```@EnabledDelayQueue``` 开启延迟队列支持

```java

@SpringBootApplication
@EnabledDelayQueue
public class DelayQueueRedisApplication {
    public static void main(String[] args) {
        SpringApplication.run(DelayQueueRedisApplication.class, args);
    }
}
```

### 2. application.properties添加配置

### 2.1. spring-data-redis接入方式

```properties
## redis相关配置，可以配置单机版redis，也可以配置集群版本。
spring.redis.host=127.0.0.1
spring.redis.port=6379
spring.redis.timeout=5000
spring.redis.lettuce.pool.max-active=50
spring.redis.lettuce.pool.max-wait=-1
spring.redis.lettuce.pool.max-idle=10
spring.redis.lettuce.pool.min-idle=0
spring.redis.lettuce.shutdown-timeout=100
## 延迟队列服务配置,除了appName，其他可以不用填写，保持系统默认配置。
delay.queue.appName=inviter-activity
delay.queue.executor.coreSize=2
delay.queue.executor.maxSize=10
delay.queue.executor.queueSize=100
delay.queue.executor.threadNamePrefix=delay-queue-consumer-worker-
delay.queue.executor.shutdownAwaitTime=10s
## 发送端相关配置，不填写，默认开启
delay.queue.p.enabled=true
## 消费端相关配置，不填写，有默认配置
delay.queue.c.enabled=true
delay.queue.c.initialDelay=3s
delay.queue.c.scanInterval=1s
delay.queue.c.shutdownAwaitTime=10s
## 抽象出来的topic主题配置，可以配置多个topic，除了 consumer-handler-name，其他均有默认配置
delay.queue.topics.inviterEventNotify.enabled=true
delay.queue.topics.inviterEventNotify.consumer-handler-name=delayQueueConsumerSkipHandler
delay.queue.topics.inviterEventNotify.consumer-exception-handler-name=delayQueueConsumerExceptionReentrantHandler
delay.queue.topics.inviterEventNotify.fetch-size=10
delay.queue.topics.inviterEventNotify.max-retry=3
delay.queue.topics.inviterEventNotify.retry-interval=5000
```

#### 2.2 jimDB接入方式

```properties
## 延迟队列服务配置,除了appName，其他可以不用填写，保持系统默认配置。
delay.queue.appName=inviter-activity
delay.queue.executor.coreSize=2
delay.queue.executor.maxSize=10
delay.queue.executor.queueSize=100
delay.queue.executor.threadNamePrefix=delay-queue-consumer-worker-
delay.queue.executor.shutdownAwaitTime=10s
## 发送端相关配置，不填写，默认开启
delay.queue.p.enabled=true
## 消费端相关配置，不填写，有默认配置
delay.queue.c.enabled=true
delay.queue.c.initialDelay=3s
delay.queue.c.scanInterval=1s
delay.queue.c.shutdownAwaitTime=10s
## 抽象出来的topic主题配置，可以配置多个topic，除了 consumer-handler-name，其他均有默认配置
delay.queue.topics.inviterEventNotify.enabled=true
delay.queue.topics.inviterEventNotify.consumer-handler-name=delayQueueConsumerSkipHandler
delay.queue.topics.inviterEventNotify.consumer-exception-handler-name=delayQueueConsumerExceptionReentrantHandler
delay.queue.topics.inviterEventNotify.fetch-size=10
delay.queue.topics.inviterEventNotify.max-retry=3
delay.queue.topics.inviterEventNotify.retry-interval=5000
```

#### 2.3 jimDB方式，配置Cluster工程类

```xml
    <!-- 京东公有云redis集群 -->
<bean id="jimClient" class="com.jd.jim.cli.ReloadableJimClientFactoryBean">
    <property name="jimUrl" value="${mvn.jimdb.url}"/>
</bean>
```

### 3. 延迟消息消费者

实现接口```DelayQueueConsumerHandler```，消费异常不在这个地方处理，有专门的异常处理类

```java
public class DelayQueueConsumerSkipHandler implements DelayQueueConsumerHandler {
    private static final Logger logger = LoggerFactory.getLogger(DelayQueueConsumerSkipHandler.class);

    @Override
    public void onMessage(DelayMessage delayMessage) {
        logger.info("消息消费：跳过处理 {}", Jsons.toJson(delayMessage));
    }
}
```

### 4. 延迟消息消费失败重试

默认实现类是 ```o2o.platform.commons.delay.queue.redis.core.handler.DelayQueueConsumerExceptionReentrantHandler```
，原理是拿到失败的消息后，二次投递到topic中，默认重试次数是3次，可以通过```delay.queue.topics.xxx.max-retry```
自定义重试次数。  

也可以实现```o2o.platform.commons.delay.queue.redis.core.handler.DelayQueueConsumerExceptionHandler```
,配置在```delay.queue.topics.xx.consumer-exception-handler-name```中。

### 5. 延迟消息发送

```java

@RestController
public class DemoController {

    private final DelayMessageProducer delayMessageProducer;

    public DemoController(DelayMessageProducer delayMessageProducer) {
        this.delayMessageProducer = delayMessageProducer;
    }


    @PostMapping("/delayMessage/send")
    public Object send(@RequestBody InviterEventNotifyMessage message,
            @RequestParam("topic") String topic,
            @RequestParam("delaySeconds") int delaySeconds) {
        return delayMessageProducer.send(
                new DelayMessage(topic, message, Duration.ofSeconds(delaySeconds)));
    }
}
```
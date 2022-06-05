## delay-queue-redis
基于redis缓存的延迟队列，内部依赖redis的数据结构(```hash zset```)实现。
通过简单的引入jar包可方便的接入。<br/>
目前支持原生的redis和jimDB(需要开启hashtag)。

## 原理
## 注意事项
适合小场景的延迟，最小延迟1S，最大延迟7天。数据容灾完全依赖底层的redis集群主从。
## 概要
具体样例请参考module```delay-queue-redis-spring-boot-starter-demo```，共四个步骤：
- 引用jar包 delay-queue-redis-[jimdb|data]-spring-boot-starter
- 启动函数添加```@EnabledDelayQueue```
- 增加application.properties相关配置，形如：生产者、消费者、topic等，大部分保持默认即可。比较重要的是```delay.queue.topics```配置。
- 增加topics对应的消费者，记得把消费者的handler名称配置到```delay.queue.topics```即可。

## 详情
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
## redis相关配置
spring.redis.host=127.0.0.1
spring.redis.port=6379
spring.redis.timeout=5000
spring.redis.lettuce.pool.max-active=50
spring.redis.lettuce.pool.max-wait=-1
spring.redis.lettuce.pool.max-idle=10
spring.redis.lettuce.pool.min-idle=0
spring.redis.lettuce.shutdown-timeout=100

## 延迟队列服务配置
delay.queue.appName=inviter-activity
delay.queue.executor.coreSize=2
delay.queue.executor.maxSize=10
delay.queue.executor.queueSize=100
delay.queue.executor.threadNamePrefix=delay-queue-consumer-worker-
delay.queue.executor.shutdownAwaitTime=10s

## 发送端相关配置 均有默认值
delay.queue.p.enabled=true

## 消费端相关配置 均有默认值
delay.queue.c.enabled=true
delay.queue.c.initialDelay=3s
delay.queue.c.scanInterval=1s
delay.queue.c.shutdownAwaitTime=10s


## 抽象出来的topic主题配置，可以配置多个topic
delay.queue.topics.inviterEventNotify.enabled=true
delay.queue.topics.inviterEventNotify.consumer-handler-name=delayQueueConsumerSkipHandler
#delay.queue.topics.inviterEventNotify.consumer-exception-handler-name=delayQueueConsumerExceptionReentrantHandler
#delay.queue.topics.inviterEventNotify.fetch-size=10
#delay.queue.topics.inviterEventNotify.max-retry=3
#delay.queue.topics.inviterEventNotify.retry-interval=5000
```

#### 2.2 jimDB接入方式
```properties
## 延迟队列服务配置
delay.queue.appName=inviter-activity
delay.queue.executor.coreSize=2
delay.queue.executor.maxSize=10
delay.queue.executor.queueSize=100
delay.queue.executor.threadNamePrefix=delay-queue-consumer-worker-
delay.queue.executor.shutdownAwaitTime=10s

## 发送端相关配置 均有默认值
delay.queue.p.enabled=true

## 消费端相关配置  均有默认值
delay.queue.c.enabled=true
delay.queue.c.initialDelay=3s
delay.queue.c.scanInterval=1s
delay.queue.c.shutdownAwaitTime=10s


## 抽象出来的topic主题配置，可以配置多个topic
delay.queue.topics.inviterEventNotify.enabled=true
delay.queue.topics.inviterEventNotify.consumer-handler-name=delayQueueConsumerSkipHandler
#delay.queue.topics.inviterEventNotify.consumer-exception-handler-name=delayQueueConsumerExceptionReentrantHandler
#delay.queue.topics.inviterEventNotify.fetch-size=10
#delay.queue.topics.inviterEventNotify.max-retry=3
#delay.queue.topics.inviterEventNotify.retry-interval=5000
```

#### 2.3 jimDB方式，配置Cluster工程类
```xml
    <!-- 京东公有云redis集群 -->
    <bean id="jimClient" class="com.jd.jim.cli.ReloadableJimClientFactoryBean">
        <property name="jimUrl" value="${mvn.jimdb.url}"/>
    </bean>
```

### 3. 消息消费者
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
### 4. 消费失败重试
默认实现类是 ```o2o.platform.commons.delay.queue.redis.core.handler.DelayQueueConsumerExceptionReentrantHandler```，原理是拿到失败的消息后，二次投递到topic中，默认重试次数是3次，可以通过```delay.queue.topics.xxx.max-retry```自定义重试次数。<br/>

也可以实现```o2o.platform.commons.delay.queue.redis.core.handler.DelayQueueConsumerExceptionHandler```,配置在```delay.queue.topics.xx.consumer-exception-handler-name```中。

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
## delay-queue-redis

基于redis缓存的延迟队列，内部依赖redis的数据结构实现。
通过简单的引入jar包可方便的接入。<br/>
目前支持原生的redis和jimDB(需要开启hashtag)。

#### 1. SpringApplication启动类头部添加```@EnabledDelayQueue`` 开启延迟队列支持
```java
@SpringBootApplication
@EnabledDelayQueue
public class DelayQueueRedisApplication {
    public static void main(String[] args) {
        SpringApplication.run(DelayQueueRedisApplication.class, args);
    }
}
```
#### 2.1 原生redis接入方式，application.properties中添加如下内容
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

## 发送端相关配置
delay.queue.producer.enabled=true

## 消费端相关配置
delay.queue.consumer.enabled=true
delay.queue.consumer.initialDelay=3s
delay.queue.consumer.scanInterval=1s
delay.queue.consumer.shutdownAwaitTime=10s


## 抽象出来的topic主题配置，可以配置多个topic
delay.queue.topics.inviterEventNotify.enabled=true
delay.queue.topics.inviterEventNotify.consumer-handler-name=delayQueueConsumerSkipHandler
#delay.queue.topics.inviterEventNotify.consumer-exception-handler-name=delayQueueConsumerExceptionReentrantHandler
#delay.queue.topics.inviterEventNotify.fetch-size=10
#delay.queue.topics.inviterEventNotify.max-retry=3
#delay.queue.topics.inviterEventNotify.retry-interval=5000
```

#### 2.2 jimDB接入方式,application.properties中新增如下内容
```properties
## 延迟队列服务配置
delay.queue.appName=inviter-activity
delay.queue.executor.coreSize=2
delay.queue.executor.maxSize=10
delay.queue.executor.queueSize=100
delay.queue.executor.threadNamePrefix=delay-queue-consumer-worker-
delay.queue.executor.shutdownAwaitTime=10s

## 发送端相关配置
delay.queue.producer.enabled=true

## 消费端相关配置
delay.queue.consumer.enabled=true
delay.queue.consumer.initialDelay=3s
delay.queue.consumer.scanInterval=1s
delay.queue.consumer.shutdownAwaitTime=10s


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
## delay-queue-redis
基于redis缓存的延迟队列，内部依赖redis的数据结构实现。
通过简单的引入jar包可方便的接入。<br/>
目前支持原生的redis和jimDB(需要开启hashtag)。

#### 1. SpringApplication启动类头部添加```@EnabledDelayQueue``` 开启延迟队列支持
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

#### 2.2 jimDB接入方式,application.properties中新增如下内容
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

#### 3. 新增消息消费者，只要实现接口```DelayQueueConsumerHandler```即可。
```java
public class DelayQueueConsumerSkipHandler implements DelayQueueConsumerHandler {
    private static final Logger logger = LoggerFactory.getLogger(DelayQueueConsumerSkipHandler.class);

    @Override
    public void onMessage(DelayMessage delayMessage) {
        logger.info("消息消费：跳过处理 {}", Jsons.toJson(delayMessage));
    }
}
```
#### 3.1 消费失败重试
默认实现类是 ```o2o.platform.commons.delay.queue.redis.core.handler.DelayQueueConsumerExceptionReentrantHandler```
也可以自定义实现```o2o.platform.commons.delay.queue.redis.core.handler.DelayQueueConsumerExceptionHandler```

#### 4. application.properties中针对设置的topic配置
handlerName为实现类的SimpleName的驼峰格式。
*消费异常有默认的实现*
```properties
## 抽象出来的topic主题配置，可以配置多个topic
delay.queue.topics.inviterEventNotify.consumer-handler-name=delayQueueConsumerSkipHandler
delay.queue.topics.inviterEventNotify.consumer-exception-handler-name=delayQueueConsumerExceptionReentrantHandler
```
#### 3. 新增消息消费者，只要实现接口```DelayQueueConsumerHandler```即可。
```java
public class DelayQueueConsumerSkipHandler implements DelayQueueConsumerHandler {
    private static final Logger logger = LoggerFactory.getLogger(DelayQueueConsumerSkipHandler.class);

    @Override
    public void onMessage(DelayMessage delayMessage) {
        logger.info("消息消费：跳过处理 {}", Jsons.toJson(delayMessage));
    }
}
```
#### 3.1 消费失败重试，默认实现类是 ```o2o.platform.commons.delay.queue.redis.core.handler.DelayQueueConsumerExceptionReentrantHandler```
也可以自定义实现```o2o.platform.commons.delay.queue.redis.core.handler.DelayQueueConsumerExceptionHandler```

#### 4. application.properties中针对设置的topic配置如下内容即可，handlerName为实现类的SimpleName的驼峰格式。
*消费异常有默认的实现*
```properties
## 抽象出来的topic主题配置，可以配置多个topic
delay.queue.topics.inviterEventNotify.consumer-handler-name=delayQueueConsumerSkipHandler
delay.queue.topics.inviterEventNotify.consumer-exception-handler-name=delayQueueConsumerExceptionReentrantHandler
```
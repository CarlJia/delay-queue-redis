package o2o.platform.commons.delay.queue.redis.core.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

import o2o.platform.commons.delay.queue.redis.core.message.DelayMessage;

/**
 * @author zhouyang01
 * Created on 2022-06-05
 */
@SuppressWarnings("ALL")
public class DelayQueueConsumerSkipHandler implements DelayQueueConsumerHandler {
    private static final Logger logger = LoggerFactory.getLogger(DelayQueueConsumerSkipHandler.class);

    @Override
    public void onMessage(DelayMessage delayMessage) {
        logger.info("消息消费：跳过处理 {}", JSON.toJSONString(delayMessage));
    }
}

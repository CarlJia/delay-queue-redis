package o2o.platform.commons.delay.queue.redis.demo;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import o2o.platform.commons.delay.queue.redis.core.message.DelayMessage;
import o2o.platform.commons.delay.queue.redis.core.service.DelayMessageProducer;

/**
 * @author zhouyang01
 * Created on 2022-06-05
 */
@RestController
public class DemoController {

    @Autowired
    private DelayMessageProducer delayMessageProducer;


    @PostMapping("/delayMessage/send")
    public Object send(@RequestBody InviterEventNotifyMessage message,
            @RequestParam("topic") String topic,
            @RequestParam("delaySeconds") int delaySeconds) {
        return delayMessageProducer.send(
                new DelayMessage(topic, Duration.ofSeconds(delaySeconds), message));
    }
}

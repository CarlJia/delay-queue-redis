package o2o.platform.commons.delay.queue.redis.demo;

import lombok.Data;

/**
 * @author zhouyang01
 * Created on 2022-06-05
 */
@Data
public class InviterEventNotifyMessage {
    private long inviterId;
    private int indicator;
}

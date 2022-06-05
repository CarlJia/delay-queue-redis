package o2o.platform.commons.delay.queue.redis.core.exception;

/**
 * 延迟消息发送异常
 *
 * @author zhouyang01
 * Created on 2022-06-04
 */
@SuppressWarnings("ALL")
public class DelayQueueSendException extends RuntimeException {

    public DelayQueueSendException() {
    }

    public DelayQueueSendException(String message) {
        super(message);
    }

    public DelayQueueSendException(String message, Throwable cause) {
        super(message, cause);
    }

    public DelayQueueSendException(Throwable cause) {
        super(cause);
    }

    public DelayQueueSendException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

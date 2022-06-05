package o2o.platform.commons.delay.queue.redis.core.properties;

import lombok.Data;

/**
 * 生产者属性对象
 * @author zhouyang01
 * Created on 2022-06-04
 */
@Data
public class ProduceProperties {

    /**
     * 是否开启生产者
     */
    private boolean enabled;

    public ProduceProperties() {
        this.enabled = true;
    }
}

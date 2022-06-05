package o2o.platform.commons.delay.queue.redis.core.domain;

import com.google.common.base.MoreObjects;

import o2o.platform.commons.delay.queue.redis.core.constants.ResultStatus;

/**
 * @author zhouyang01
 * Created on 2022-06-04
 */
public class SendResult {
    private ResultStatus resultStatus;
    private String messageId;
    private String tips;

    public static SendResult success(String messageId) {
        SendResult result = new SendResult();
        result.setResultStatus(ResultStatus.SEND_SUCCESS);
        result.setMessageId(messageId);
        result.setTips("send success");
        return result;
    }

    public static SendResult failure(ResultStatus resultStatus, String tips) {
        SendResult result = new SendResult();
        result.setResultStatus(resultStatus);
        result.setTips(tips);
        return result;
    }

    public ResultStatus getResultStatus() {
        return resultStatus;
    }

    public void setResultStatus(ResultStatus resultStatus) {
        this.resultStatus = resultStatus;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getTips() {
        return tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .omitNullValues()
                .add("resultStatus", this.resultStatus)
                .add("messageId", this.messageId)
                .add("tips", this.tips)
                .toString();
    }
}

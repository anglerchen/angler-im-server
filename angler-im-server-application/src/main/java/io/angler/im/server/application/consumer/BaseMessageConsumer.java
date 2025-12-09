package io.angler.im.server.application.consumer;

import com.alibaba.fastjson.JSONObject;
import io.angler.im.common.domain.constants.IMConstants;
import io.angler.im.common.domain.model.IMReceiveInfo;

public class BaseMessageConsumer {
    /**
     * 解析数据
     */
    protected IMReceiveInfo getReceiveMessage(String msg){
        JSONObject jsonObject = JSONObject.parseObject(msg);
        String eventStr = jsonObject.getString(IMConstants.MSG_KEY);
        return JSONObject.parseObject(eventStr, IMReceiveInfo.class);
    }
}


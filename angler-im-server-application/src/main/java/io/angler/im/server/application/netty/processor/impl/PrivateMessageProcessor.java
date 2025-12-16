package io.angler.im.server.application.netty.processor.impl;

import com.jc.angler.im.common.domain.enums.IMCmdType;
import com.jc.angler.im.common.domain.enums.IMSendCode;
import com.jc.angler.im.common.domain.model.IMReceiveInfo;
import com.jc.angler.im.common.domain.model.IMSendInfo;
import com.jc.angler.im.common.domain.model.IMUserInfo;
import com.jc.angler.im.common.mq.MessageSenderService;
import io.angler.im.server.application.netty.cache.UserChannelContextCache;
import io.angler.im.server.application.netty.processor.MessageProcessor;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PrivateMessageProcessor extends BaseMessageProcessor implements MessageProcessor<IMReceiveInfo> {
    private final Logger logger = LoggerFactory.getLogger(PrivateMessageProcessor.class);

    @Autowired
    private MessageSenderService messageSenderService;

    @Override
    public void process(IMReceiveInfo receiveInfo) {
        IMUserInfo sender = receiveInfo.getSender();
        IMUserInfo receiver = receiveInfo.getReceivers().get(0);
        logger.info("PrivateMessageProcessor.process|接收到消息,发送者:{}, 接收者:{}, 内容:{}", sender.getUserId(), receiver.getUserId(), receiveInfo.getData());
        try{
            ChannelHandlerContext channelHandlerContext = UserChannelContextCache.getChannelCtx(receiver.getUserId(), receiver.getTerminal());
            if (channelHandlerContext != null){
                //推送消息
                IMSendInfo<?> imSendInfo = new IMSendInfo<>(IMCmdType.PRIVATE_MESSAGE.code(), receiveInfo.getData());
                channelHandlerContext.writeAndFlush(imSendInfo);
                sendPrivateMessageResult(receiveInfo, IMSendCode.SUCCESS);
            }else{
                sendPrivateMessageResult(receiveInfo, IMSendCode.NOT_FIND_CHANNEL);
                logger.error("PrivateMessageProcessor.process|未找到Channel, 发送者:{}, 接收者:{}, 内容:{}", sender.getUserId(), receiver.getUserId(), receiveInfo.getData());
            }
        }catch (Exception e){
            sendPrivateMessageResult(receiveInfo, IMSendCode.UNKONW_ERROR);
            logger.error("PrivateMessageProcessor.process|发送异常,发送者:{}, 接收者:{}, 内容:{}, 异常信息:{}", sender.getUserId(), receiver.getUserId(), receiveInfo.getData(), e.getMessage());
        }
    }
}


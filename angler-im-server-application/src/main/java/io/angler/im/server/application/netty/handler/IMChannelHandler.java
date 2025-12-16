package io.angler.im.server.application.netty.handler;

import com.jc.angler.im.common.cache.distribute.DistributedCacheService;
import com.jc.angler.im.common.domain.constants.IMConstants;
import com.jc.angler.im.common.domain.enums.IMCmdType;
import com.jc.angler.im.common.domain.model.IMSendInfo;
import io.angler.im.server.application.netty.cache.UserChannelContextCache;
import io.angler.im.server.application.netty.processor.MessageProcessor;
import io.angler.im.server.application.netty.processor.factory.ProcessorFactory;
import io.angler.im.server.infrastructure.holder.SpringContextHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IMChannelHandler extends SimpleChannelInboundHandler<IMSendInfo> {

    private final Logger logger = LoggerFactory.getLogger(IMChannelHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, IMSendInfo imSendInfo) throws Exception {
        MessageProcessor processor = ProcessorFactory.getProcessor(IMCmdType.fromCode(imSendInfo.getCmd()));
        processor.process(ctx, processor.transForm(imSendInfo.getData()));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("IMChannelHandler.exceptionCaught|异常:{}", cause.getMessage());
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        logger.info("IMChannelHandler.handlerAdded|{}连接", ctx.channel().id().asLongText());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        AttributeKey<Long> userIdAttr = AttributeKey.valueOf(IMConstants.USER_ID);
        Long userId = ctx.channel().attr(userIdAttr).get();

        AttributeKey<Integer> terminalAttr = AttributeKey.valueOf(IMConstants.TERMINAL_TYPE);
        Integer terminal = ctx.channel().attr(terminalAttr).get();

        ChannelHandlerContext channelCtx = UserChannelContextCache.getChannelCtx(userId, terminal);
        // 防止异地登录误删
        if (channelCtx != null && channelCtx.channel().id().equals(ctx.channel().id())){
            UserChannelContextCache.removeChannelCtx(userId, terminal);
            DistributedCacheService distributedCacheService = SpringContextHolder.getBean(IMConstants.DISTRIBUTED_CACHE_REDIS_SERVICE_KEY);
            String redisKey = String.join(IMConstants.REDIS_KEY_SPLIT, IMConstants.IM_USER_SERVER_ID, userId.toString(), terminal.toString());
            distributedCacheService.delete(redisKey);
            logger.info("IMChannelHandler.handlerRemoved|断开连接, userId:{}, 终端类型:{}", userId, terminal);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent){
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE){
                AttributeKey<Long> attr = AttributeKey.valueOf(IMConstants.USER_ID);
                Long userId = ctx.channel().attr(attr).get();

                AttributeKey<Integer> terminalAttr = AttributeKey.valueOf(IMConstants.TERMINAL_TYPE);
                Integer terminal = ctx.channel().attr(terminalAttr).get();
                logger.info("IMChannelHandler.userEventTriggered|心跳超时.即将断开连接, userId:{}, 终端类型:{}", userId, terminal);
                ctx.channel().close();
            }
        }else {
            super.userEventTriggered(ctx, evt);
        }
    }
}


package io.angler.im.server.application.netty.processor;

import io.netty.channel.ChannelHandlerContext;

public interface MessageProcessor<T> {

    /**
     * 处理数据
     */
    default void process(ChannelHandlerContext ctx, T data){

    }

    /**
     * 处理数据
     */
    default void process(T data){

    }

    /**
     * 转化数据
     */
    default T transForm(Object obj){
        return (T) obj;
    }
}


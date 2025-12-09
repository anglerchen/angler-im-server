package io.angler.im.server.application.netty;

public interface IMNettyServer {
    /**
     * 是否已就绪
     */
    boolean isReady();

    /**
     * 启动服务
     */
    void start();

    /**
     * 停止服务
     */
    void shutdown();
}



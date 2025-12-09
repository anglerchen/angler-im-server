package io.angler.im.server.application.netty.runner;

import cn.hutool.core.collection.CollectionUtil;
import io.angler.im.server.application.netty.IMNettyServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.List;

@Component
public class IMServerRunner implements CommandLineRunner {

    @Autowired
    private List<IMNettyServer> imNettyServers;

    /**
     * 判断服务是否准备完毕
     */
    public boolean isReady(){
       for (IMNettyServer imNettyServer : imNettyServers){
           if (!imNettyServer.isReady()){
               return false;
           }
       }
       return true;
    }

    @Override
    public void run(String... args) throws Exception {
        //启动每个服务
        if (!CollectionUtil.isEmpty(imNettyServers)){
            imNettyServers.forEach(IMNettyServer::start);
        }
    }

    @PreDestroy
    public void destroy(){
        if (!CollectionUtil.isEmpty(imNettyServers)){
            imNettyServers.forEach(IMNettyServer::shutdown);
        }
    }
}


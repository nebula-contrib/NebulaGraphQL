package com.nebulagraphql.util;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.vesoft.nebula.client.graph.NebulaPoolConfig;
import com.vesoft.nebula.client.graph.SessionPool;
import com.vesoft.nebula.client.graph.SessionPoolConfig;
import com.vesoft.nebula.client.graph.data.HostAddress;
import com.vesoft.nebula.client.graph.exception.AuthFailedException;
import com.vesoft.nebula.client.graph.exception.BindSpaceFailedException;
import com.vesoft.nebula.client.graph.exception.ClientServerIncompatibleException;
import com.vesoft.nebula.client.graph.exception.IOErrorException;
import com.vesoft.nebula.client.graph.exception.InvalidConfigException;
import com.vesoft.nebula.client.graph.exception.NotValidConnectionException;
import com.vesoft.nebula.client.graph.net.NebulaPool;
import com.vesoft.nebula.client.graph.net.Session;

public class InitialUtil {
    private static final Logger logger = LoggerFactory.getLogger(InitialUtil.class);

    public static void initialBasketballPlayer(){
        logger.info("creating space");
        createSpace();
        timeLine(5000);
        logger.info("create space success");
        List<HostAddress> addresses = Arrays.asList(new HostAddress("graphd", 9669));
        String spaceName = "basketballplayer";
        String user = "root";
        String password = "nebula";
        SessionPoolConfig sessionPoolConfig = new SessionPoolConfig(addresses, spaceName, user, password);
        SessionPool sessionPool = new SessionPool(sessionPoolConfig);
        if (!sessionPool.init()) {
            throw new RuntimeException();
        }
        try {
            logger.info("creating tag and edge");
            sessionPool.execute("create tag player(name string,age int);");
            sessionPool.execute("create tag team(name string);");
            sessionPool.execute("create edge serve(start_year int,end_year int);");
            sessionPool.execute("create edge follow(degree int);");
            timeLine(5000);
            logger.info("create tag and edge success");
            logger.info("creating index");
            sessionPool.execute("create tag index player_index_0 on player();");
            sessionPool.execute("create tag index player_index_1 on player(name(20));");
            sessionPool.execute("create tag index player_index_2 on player(age);");
            timeLine(20000);
            logger.info("create index success");
        } catch (IOErrorException | ClientServerIncompatibleException | AuthFailedException
                | BindSpaceFailedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String fileName = "basketballplayer.ngql";
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        logger.info("inserting vertices and edges");
        try (Stream<String> stream = Files.lines(Paths.get(classLoader.getResource(fileName).toURI()))) {
                stream.forEach(statement->{
                    try {
                        sessionPool.execute(statement);
                    } catch (IOErrorException | ClientServerIncompatibleException | AuthFailedException
                            | BindSpaceFailedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                });
         } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            System.exit(1);
        } finally{
            sessionPool.close();
        }
        logger.info("insert vertices and edges success");
    }

    private static void createSpace(){
        NebulaPoolConfig nebulaPoolConfig = new NebulaPoolConfig();
        nebulaPoolConfig.setMaxConnSize(10);
        List<HostAddress> addresses = Arrays.asList(new HostAddress("graphd", 9669));
        NebulaPool pool = new NebulaPool();
        try {
            pool.init(addresses, nebulaPoolConfig);
        } catch (UnknownHostException | InvalidConfigException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Session session;
        try {
            session = pool.getSession("root", "nebula", false);
            session.execute("drop space basketballplayer;");
            session.execute("create space basketballplayer(partition_num=10,replica_factor=1,vid_type=fixed_string(32));");
            session.release();
        } catch (NotValidConnectionException | IOErrorException | AuthFailedException
                | ClientServerIncompatibleException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        pool.close();
    }

    private static void timeLine(int sleepTime){
        for(int i = 0;i<=sleepTime/1000;i++){
            int percent = i*100/(sleepTime/1000);
            String process = "["+Strings.repeat("=", percent/2)+Strings.repeat(" ", 50-percent/2)+"]";
            System.out.println(process+" "+percent+"%");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}

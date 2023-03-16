package com.nebulagraphql;

import com.nebulagraphql.rsboot.ResultSetBoot;
import com.vesoft.nebula.client.graph.SessionPool;
import com.vesoft.nebula.client.graph.SessionPoolConfig;
import com.vesoft.nebula.client.graph.data.HostAddress;
import com.vesoft.nebula.client.graph.data.ResultSet;
import com.vesoft.nebula.client.graph.exception.AuthFailedException;
import com.vesoft.nebula.client.graph.exception.BindSpaceFailedException;
import com.vesoft.nebula.client.graph.exception.ClientServerIncompatibleException;
import com.vesoft.nebula.client.graph.exception.IOErrorException;

import java.util.Arrays;
import java.util.List;

public class NebulaHelloWorld {
    public static void main(String[] args) {
        List<HostAddress> addresses = Arrays.asList(new HostAddress("127.0.0.1", 9669));
        String spaceName = "demo_basketballplayer";
        String user = "root";
        String password = "nebula";
        SessionPoolConfig sessionPoolConfig = new SessionPoolConfig(addresses, spaceName, user, password);
        SessionPool sessionPool = new SessionPool(sessionPoolConfig);
        if (!sessionPool.init()) {
            return;
        }
        ResultSet resultSet;
        try {
            resultSet = sessionPool.execute("LOOKUP ON player WHERE player.age == 32 yield id(vertex) as vertexId | FETCH PROP ON player $-.vertexId YIELD vertex AS v;");
            System.out.println(ResultSetBoot.wrap(resultSet).getVertices());
        } catch (IOErrorException | ClientServerIncompatibleException | AuthFailedException | BindSpaceFailedException e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            sessionPool.close();
        }
    }
}

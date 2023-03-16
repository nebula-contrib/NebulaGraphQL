package com.nebulagraphql;

import com.nebulagraphql.rsboot.ResultSetBoot;
import com.nebulagraphql.rsboot.domain.Vertex;
import com.vesoft.nebula.client.graph.SessionPool;
import com.vesoft.nebula.client.graph.SessionPoolConfig;
import com.vesoft.nebula.client.graph.data.HostAddress;
import com.vesoft.nebula.client.graph.data.ResultSet;
import com.vesoft.nebula.client.graph.exception.AuthFailedException;
import com.vesoft.nebula.client.graph.exception.BindSpaceFailedException;
import com.vesoft.nebula.client.graph.exception.ClientServerIncompatibleException;
import com.vesoft.nebula.client.graph.exception.IOErrorException;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PlayerDataFetcher implements DataFetcher<Object> {
    @Override
    public Object get(DataFetchingEnvironment environment) throws Exception {
        int age = environment.getArgument("age");
        List<HostAddress> addresses = Arrays.asList(new HostAddress("127.0.0.1", 9669));
        String spaceName = "demo_basketballplayer";
        String user = "root";
        String password = "nebula";
        SessionPoolConfig sessionPoolConfig = new SessionPoolConfig(addresses, spaceName, user, password);
        SessionPool sessionPool = new SessionPool(sessionPoolConfig);
        if (!sessionPool.init()) {
            throw new RuntimeException();
        }
        ResultSet resultSet;
        try {
            resultSet = sessionPool.execute("LOOKUP ON player WHERE player.age == "+age+" yield id(vertex) as vertexId | FETCH PROP ON player $-.vertexId YIELD vertex AS v;");
            List<Vertex> vertices = ResultSetBoot.wrap(resultSet).getVertices();
            List<Map<String,Object>> res = vertices.stream().map(vertex -> vertex.getTags().get(0).getProperties()).collect(Collectors.toList());
            return res;
        } catch (IOErrorException | ClientServerIncompatibleException | AuthFailedException | BindSpaceFailedException e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            sessionPool.close();
        }
        return null;
    }
}

package com.nebulagraphql.schema;

import com.nebulagraphql.ngql.DataProcessor;
import com.nebulagraphql.ngql.DataProcessorImpl;
import com.nebulagraphql.ngql.GetVerticesByProperty;
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
import graphql.schema.GraphQLFieldDefinition;

import java.util.*;
import java.util.stream.Collectors;

public class NebulaDataFetcher implements DataFetcher<Object> {
    @Override
    public Object get(DataFetchingEnvironment environment) throws Exception {
        System.out.println("in nebula data fetcher");
        GraphQLFieldDefinition fieldDefinition = environment.getFieldDefinition();
        String field = fieldDefinition.getName();
        String tagName = field.substring(0,field.length()-1);
        DataProcessor dataProcessor = new DataProcessorImpl("demo_basketballplayer",tagName);
        Map<String,Object> arguments = environment.getArguments();
        System.out.println(arguments);
        Map<String,String> properties = new HashMap<>();
        for(Map.Entry<String,Object> argument:arguments.entrySet()){
            if(argument.getValue()!=null){
                properties.put(argument.getKey(),argument.getValue().toString());
            }
        }
        String statement = new GetVerticesByProperty(tagName,properties).toQuery();
        System.out.println(statement);
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
            resultSet = sessionPool.execute(statement);
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

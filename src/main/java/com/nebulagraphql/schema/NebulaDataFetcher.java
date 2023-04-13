package com.nebulagraphql.schema;

import com.nebulagraphql.ngql.DataProcessor;
import com.nebulagraphql.ngql.DataProcessorImpl;
import com.nebulagraphql.ngql.GetVerticesByProperty;
import com.nebulagraphql.rsboot.ResultSetBoot;
import com.nebulagraphql.rsboot.domain.Vertex;
import com.nebulagraphql.session.GraphqlSessionPool;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NebulaDataFetcher implements DataFetcher<Object> {
    private static final Logger logger = LoggerFactory.getLogger(NebulaDataFetcher.class);

    private String space;
    private final SessionPool sessionPool;

    public NebulaDataFetcher(String space,SessionPool sessionPool){
        this.space = space;
        this.sessionPool = sessionPool;
    }

    @Override
    public Object get(DataFetchingEnvironment environment) throws Exception {
        GraphQLFieldDefinition fieldDefinition = environment.getFieldDefinition();
        String field = fieldDefinition.getName();
        String tagName = field.substring(0,field.length()-1);
        DataProcessor dataProcessor = new DataProcessorImpl(space,tagName);
        Map<String,Object> arguments = environment.getArguments();
        logger.debug("arguments:{}",arguments);
        Map<String,String> properties = new HashMap<>();
        for(Map.Entry<String,Object> argument:arguments.entrySet()){
            if(argument.getValue()!=null){
                properties.put(
                    argument.getKey(),
                    dataProcessor.process(argument.getKey(), argument.getValue().toString())
                );
            }
        }
        String statement = new GetVerticesByProperty(tagName,properties).toQuery();
        logger.debug(statement);
        ResultSet resultSet;
        try {
            resultSet = sessionPool.execute(statement);
            if(!resultSet.isSucceeded()){
                logger.error(resultSet.getErrorMessage());
            }
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

package com.nebulagraphql;

import com.google.common.collect.Lists;
import com.nebulagraphql.schema.SchemaManger;
import com.nebulagraphql.session.GraphqlSessionPool;
import com.nebulagraphql.session.GraphqlSessionPoolConfig;
import com.nebulagraphql.util.InitialUtil;
import com.vesoft.nebula.client.graph.data.HostAddress;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaPrinter;

import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoGenerateMain {
    private static final Logger logger = LoggerFactory.getLogger(AutoGenerateMain.class);
    public static void main(String[] args) throws UnknownHostException {
        logger.info("Test");
        InitialUtil.initialBasketballPlayer();
        HostAddress metadAddress = new HostAddress("metad0",9559);
        HostAddress graphdAddress = new HostAddress("graphd", 9669);
        String spaceName = "basketballplayer";
        String username = "root";
        String password = "nebula";
        GraphqlSessionPoolConfig graphqlSessionPoolConfig = new GraphqlSessionPoolConfig(
            Lists.newArrayList(graphdAddress), 
            Lists.newArrayList(metadAddress), 
            spaceName, username, password);
        graphqlSessionPoolConfig.setTimeout(3000);
        GraphqlSessionPool pool = new GraphqlSessionPool(graphqlSessionPoolConfig);
        ExecutionResult executionResult = pool.execute("{players(age:32){name\nage}}");
        System.out.println(executionResult.getData().toString());
        ExecutionResult executionResult2 = pool.execute("{players(name:\"Kobe Bryant\"){name\nage}}");
        System.out.println(executionResult2.getData().toString());
    }
}

package com.nebulagraphql;

import com.google.common.collect.Lists;
import com.nebulagraphql.schema.SchemaManger;
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
        HostAddress hostAddress = new HostAddress("metad0",9559);
        System.out.println("connect to metad");
        SchemaManger schemaManger = new SchemaManger(Lists.newArrayList(hostAddress));
        GraphQLSchema graphQLSchema = schemaManger.generateSchema("basketballplayer");
        SchemaPrinter schemaPrinter = new SchemaPrinter();
        String printer = schemaPrinter.print(graphQLSchema);
        System.out.println(printer);
        GraphQL build = GraphQL.newGraphQL(graphQLSchema).build();
        ExecutionResult executionResult = build.execute("{players(age:32){name\nage}}");
        System.out.println(executionResult.getData().toString());
        ExecutionResult executionResult2 = build.execute("{players(name:\"Kobe Bryant\"){name\nage}}");
        System.out.println(executionResult2.getData().toString());
    }
}

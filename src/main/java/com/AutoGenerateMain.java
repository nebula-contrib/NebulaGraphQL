package com;

import com.google.common.collect.Lists;
import com.nebulagraphql.schema.SchemaManger;
import com.vesoft.nebula.client.graph.data.HostAddress;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaPrinter;

import java.net.UnknownHostException;

public class AutoGenerateMain {
    public static void main(String[] args) throws UnknownHostException {
        HostAddress hostAddress = new HostAddress("127.0.0.1",64085);
        SchemaManger schemaManger = new SchemaManger(Lists.newArrayList(hostAddress));
        GraphQLSchema graphQLSchema = schemaManger.generateSchema("demo_basketballplayer");
        SchemaPrinter schemaPrinter = new SchemaPrinter();
        String printer = schemaPrinter.print(graphQLSchema);
        System.out.println(printer);
        GraphQL build = GraphQL.newGraphQL(graphQLSchema).build();
        ExecutionResult executionResult = build.execute("{players(age:32){name\nage}}");
        System.out.println(executionResult.getData().toString());
    }
}

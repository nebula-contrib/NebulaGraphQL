package com.nebulagraphql;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.Scalars;
import graphql.schema.*;
import graphql.schema.idl.SchemaPrinter;

import static graphql.schema.FieldCoordinates.coordinates;
import static graphql.schema.GraphQLArgument.newArgument;
import static graphql.schema.GraphQLCodeRegistry.newCodeRegistry;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;

public class Main {
    public static void main(String[] args) {
        //创建基本类型
        GraphQLObjectType playerType = newObject()
                .name("Player")// tag
                .field(newFieldDefinition()
                        .name("id") // 固定名称id
                        .type(Scalars.GraphQLString))
                .field(newFieldDefinition()
                        .name("name") //顶点属性1
                        .type(Scalars.GraphQLString)) //根据schema做映射
                .field(newFieldDefinition()
                        .name("age") //顶点属性2
                        .type(Scalars.GraphQLInt))
                .build();

        //创建query
        GraphQLObjectType queryType = newObject()
                .name("Query")
                .field(newFieldDefinition()
                        .name("players") // 根据tag名称来指定
                        .type(GraphQLList.list(playerType)) //应该都是list，但是值可以为null
                        .argument(newArgument()
                                .name("age") // argument是否可以做到任意可选组合？
                                .type(Scalars.GraphQLInt)))
                .build();
        DataFetcher<Object> dataFetcher = new PlayerDataFetcher();
        GraphQLCodeRegistry codeRegistry = newCodeRegistry()
                .dataFetcher(
                        coordinates("Query","players"),
                        dataFetcher
                ).build();
        GraphQLSchema graphQLSchema = GraphQLSchema.newSchema()
                .query(queryType)
                .codeRegistry(codeRegistry)
                .build();
        SchemaPrinter schemaPrinter = new SchemaPrinter();
        String printer = schemaPrinter.print(graphQLSchema);
        System.out.println(printer);
        GraphQL build = GraphQL.newGraphQL(graphQLSchema).build();
        ExecutionResult executionResult = build.execute("{players(age:32){name}}");
        System.out.println(executionResult.getData().toString());
    }
}

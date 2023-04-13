package com.nebulagraphql.session;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import com.nebulagraphql.schema.SchemaManger;
import com.vesoft.nebula.client.graph.SessionPool;
import com.vesoft.nebula.client.graph.SessionPoolConfig;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphqlSessionPool {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final SessionPool sessionPool;
    private final GraphQL build;
    private final GraphQLSchema graphQLSchema;

    public GraphqlSessionPool(GraphqlSessionPoolConfig config) throws UnknownHostException{
        sessionPool = new SessionPool(config.getSessionPoolConfig());
        SchemaManger schemaManger = new SchemaManger(config.getMetadAddress());
        graphQLSchema = schemaManger.generateSchema(config.getSpaceName(),sessionPool);
        build = GraphQL.newGraphQL(graphQLSchema).build();
    }


}

package com.nebulagraphql.session;

import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.facebook.thrift.TException;
import com.nebulagraphql.schema.SchemaManger;
import com.vesoft.nebula.PropertyType;
import com.vesoft.nebula.client.graph.SessionPool;
import com.vesoft.nebula.client.graph.SessionPoolConfig;
import com.vesoft.nebula.client.graph.data.HostAddress;
import com.vesoft.nebula.client.graph.exception.ClientServerIncompatibleException;
import com.vesoft.nebula.client.meta.MetaClient;
import com.vesoft.nebula.client.meta.exception.ExecuteFailedException;
import com.vesoft.nebula.meta.ColumnDef;
import com.vesoft.nebula.meta.Schema;
import com.vesoft.nebula.meta.TagItem;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphqlSessionPool {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final SessionPool sessionPool;
    private final GraphQL build;
    private final GraphQLSchema graphQLSchema;

    private final MetaData metaData;

    public GraphqlSessionPool(GraphqlSessionPoolConfig config) throws UnknownHostException{
        this.metaData = getMetaData(config.getMetadAddress(), config.getSpaceName(), config.getTimeout(), 3, 3);
        
        sessionPool = new SessionPool(config.getSessionPoolConfig());
        SchemaManger schemaManger = new SchemaManger(config.getMetadAddress());
        graphQLSchema = schemaManger.generateSchema(config.getSpaceName(),sessionPool);
        build = GraphQL.newGraphQL(graphQLSchema).build();
    }

    private MetaData getMetaData(List<HostAddress> addresses,String spaceName,int timeout,int connectionRetry,int executionRetry) throws UnknownHostException{
        MetaClient client = new MetaClient(addresses,timeout,connectionRetry,executionRetry);
        try {
            client.connect();
            List<TagItem> tags = client.getTags(spaceName);
            Map<String,Map<String,PropertyType>> tagsFileds = getTagsFields(tags);
            return new MetaData(tagsFileds);
        } catch (TException | ClientServerIncompatibleException | ExecuteFailedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }

    private String decodeString(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private Map<String, PropertyType> getColumnMap(Schema schema) {
        return schema.getColumns().stream().collect(Collectors.toMap(
            column -> decodeString(column.getName()),
            column -> column.type.getType()
        ));
    }

    private Map<String, Map<String, PropertyType>> getTagsFields(List<TagItem> tags) {
        return tags.stream().collect(Collectors.toMap(
            tag -> decodeString(tag.getTag_name()),
            tag -> getColumnMap(tag.getSchema())
        ));
    }

}

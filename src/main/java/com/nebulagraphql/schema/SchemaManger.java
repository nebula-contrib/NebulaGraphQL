package com.nebulagraphql.schema;

import com.nebulagraphql.session.MetaData;
import com.nebulagraphql.util.SchemaUtils;
import com.vesoft.nebula.PropertyType;
import com.vesoft.nebula.client.graph.SessionPool;
import com.vesoft.nebula.client.graph.data.HostAddress;
import com.vesoft.nebula.client.graph.exception.ClientServerIncompatibleException;
import com.vesoft.nebula.client.meta.MetaClient;
import com.vesoft.nebula.client.meta.exception.ExecuteFailedException;
import com.vesoft.nebula.meta.ColumnDef;
import com.vesoft.nebula.meta.Schema;
import com.vesoft.nebula.meta.TagItem;
import graphql.Scalars;
import graphql.language.NullValue;
import graphql.schema.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class SchemaManger {
    private static final Logger logger = LoggerFactory.getLogger(SchemaManger.class);

    private final MetaClient metaClient;

    private GraphQLObjectType.Builder query = GraphQLObjectType.newObject();

    private GraphQLCodeRegistry.Builder codeRegistry = GraphQLCodeRegistry.newCodeRegistry();

    public SchemaManger(List<HostAddress> addresses) throws UnknownHostException {
        this.metaClient = new MetaClient(addresses, 30000, 3, 3);
    }

    public GraphQLSchema generateSchema(String space, SessionPool sessionPool, MetaData metaData) {
        logger.debug("Generating graphql schema from space: {}", space);
        DataFetcher<Object> propertyDataFetcher = new NebulaDataFetcher(sessionPool, metaData);
        try {
            metaClient.connect();
            List<TagItem> tags = metaClient.getTags(space);
            query.name("Query");
            for (TagItem tag : tags) {

            }
            GraphQLObjectType queryType = query.build();
            GraphQLSchema graphQLSchema = GraphQLSchema.newSchema()
                    .query(queryType)
                    .codeRegistry(codeRegistry.build())
                    .build();
            logger.debug("Generate graphql schema from space success, space name: {}", space);
            return graphQLSchema;
        } catch (ClientServerIncompatibleException e) {
            throw new RuntimeException(e);
        } catch (ExecuteFailedException e) {
            throw new RuntimeException(e);
        } finally {
            metaClient.close();
        }
    }

    public void generateTagSchema(TagItem tag,DataFetcher<Object> propertyDataFetcher){
        String tagName = new String(tag.getTag_name(), StandardCharsets.UTF_8);
        logger.debug("Generating schema for tag: {}", tagName);
        GraphQLObjectType tagType = generateTagType(tag);
        List<GraphQLArgument> arguments = generateArguments(tag);
        //add query for vertices according to properties
        query.field(GraphQLFieldDefinition.newFieldDefinition()
                .name(tagName + "s")
                .type(GraphQLNonNull.nonNull(GraphQLList.list(tagType)))// if there is no matching vertex, return empty list
                .arguments(arguments)
                .build());
        //add query for specific vertex according to VID
        query.field(GraphQLFieldDefinition.newFieldDefinition()
                .name(tagName)
                .type(tagType)// result will be null if VID not exist
                .argument(GraphQLArgument.newArgument()
                        .name("ID")
                        .type(Scalars.GraphQLID)
                        .description("Vertex ID")
                        .build())
                .build());
        codeRegistry.dataFetcher(FieldCoordinates.coordinates("Query", tagName + "s"), propertyDataFetcher);
        logger.debug("Generate tag schema success, tagName: {}", tagName);
    }

    
    public GraphQLObjectType generateTagType(TagItem tag){
        Schema schema = tag.getSchema();
        String tagName = new String(tag.getTag_name(), StandardCharsets.UTF_8);
        GraphQLObjectType.Builder tagTypeBuilder = GraphQLObjectType.newObject();
        tagTypeBuilder.name(tagName);
        for (ColumnDef columnDef : schema.getColumns()) {
            GraphQLFieldDefinition.Builder fieldDefinitionBuilder = GraphQLFieldDefinition.newFieldDefinition();
            String fieldName = new String(columnDef.getName(), StandardCharsets.UTF_8);
            GraphQLScalarType scalarType = SchemaUtils.getType(columnDef.type.getType());
            fieldDefinitionBuilder.name(fieldName)
                    .type(scalarType);
            byte[] desc = columnDef.getComment();
            if (desc != null) {
                fieldDefinitionBuilder.description(new String(desc, StandardCharsets.UTF_8));
            }
            tagTypeBuilder.field(fieldDefinitionBuilder);
        }
        return tagTypeBuilder.build();
    }

    public List<GraphQLArgument> generateArguments(TagItem tag){
        return tag.schema.columns.stream().map(this::generateArgument).collect(Collectors.toList());
    }

    public GraphQLArgument generateArgument(ColumnDef columnDef){
        String fieldName = new String(columnDef.getName(), StandardCharsets.UTF_8);
        GraphQLScalarType scalarType = SchemaUtils.getType(columnDef.type.getType());
        GraphQLArgument.Builder argumentBuilder = GraphQLArgument.newArgument();
        argumentBuilder.name(fieldName).type(scalarType).defaultValueLiteral(NullValue.of());
        byte[] desc = columnDef.getComment();
        if (desc != null) {
            argumentBuilder.description(new String(desc, StandardCharsets.UTF_8));
        }
        return argumentBuilder.build();
    }
}

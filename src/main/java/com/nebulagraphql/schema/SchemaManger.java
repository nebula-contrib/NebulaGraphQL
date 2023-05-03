package com.nebulagraphql.schema;

import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nebulagraphql.session.MetaData;
import com.nebulagraphql.util.SchemaUtils;
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
import graphql.schema.DataFetcher;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;

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
            for (TagItem tag : tags) {
                generateTagSchema(tag, propertyDataFetcher);
            }
            return generateGraphQLSchema();
        } catch (ClientServerIncompatibleException e) {
            throw new RuntimeException(e);
        } catch (ExecuteFailedException e) {
            throw new RuntimeException(e);
        } finally {
            metaClient.close();
        }
    }

    private GraphQLSchema generateGraphQLSchema() {
        this.query.name("Query");
        GraphQLObjectType queryType = this.query.build();
        GraphQLSchema graphQLSchema = GraphQLSchema.newSchema()
                .query(queryType)
                .codeRegistry(codeRegistry.build())
                .build();
        return graphQLSchema;
    }

    private void generateTagSchema(TagItem tag, DataFetcher<Object> propertyDataFetcher) {
        String tagName = decode(tag.getTag_name());
        logger.debug("Generating schema for tag: {}", tagName);
        GraphQLObjectType tagType = generateTagType(tag);
        List<GraphQLArgument> arguments = generateArguments(tag);
        // add query for vertices according to properties
        query.field(GraphQLFieldDefinition.newFieldDefinition()
                .name(tagName + "s")
                .type(GraphQLNonNull.nonNull(GraphQLList.list(tagType)))// if there is no matching vertex, return empty
                                                                        // list
                .arguments(arguments)
                .build());
        // add query for specific vertex according to VID
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

    /**
     * Generate GraphQLObjectType for tag
     * 
     * @param tag
     * @return 
     * e.g. 
     * <p>type player {</p>
     * <p> age: Int </p>
     * <p>name: String </p>
     * <p>}</p>
     */
    private GraphQLObjectType generateTagType(TagItem tag) {
        String tagName = decode(tag.getTag_name());
        GraphQLObjectType.Builder tagTypeBuilder = GraphQLObjectType.newObject();
        tagTypeBuilder.name(tagName);
        Schema schema = tag.getSchema();
        for (ColumnDef columnDef : schema.getColumns()) {
            GraphQLFieldDefinition.Builder fieldDefinitionBuilder = GraphQLFieldDefinition.newFieldDefinition();
            String fieldName = decode(columnDef.getName());
            GraphQLScalarType scalarType = SchemaUtils.getType(columnDef.type.getType());
            fieldDefinitionBuilder.name(fieldName)
                    .type(scalarType);
            byte[] desc = columnDef.getComment();
            if (desc != null) {
                fieldDefinitionBuilder.description(decode(desc));
            }
            tagTypeBuilder.field(fieldDefinitionBuilder);
        }
        return tagTypeBuilder.build();
    }

    /**
     * Generate arguments for each tag query
     * 
     * @param tag
     * @return
     */
    public List<GraphQLArgument> generateArguments(TagItem tag) {
        return tag.schema.columns.stream().map(this::generateArgument).collect(Collectors.toList());
    }

    /**
     * Generate argument for each field
     * default value is null
     * if value is null, it will be ignored in data fetcher
     * 
     * @param columnDef
     * @return
     */
    public GraphQLArgument generateArgument(ColumnDef columnDef) {
        String fieldName = decode(columnDef.getName());
        GraphQLScalarType scalarType = SchemaUtils.getType(columnDef.type.getType());
        GraphQLArgument.Builder argumentBuilder = GraphQLArgument.newArgument();
        argumentBuilder.name(fieldName).type(scalarType).defaultValueLiteral(NullValue.of());
        byte[] desc = columnDef.getComment();
        if (desc != null) {
            argumentBuilder.description(decode(desc));
        }
        return argumentBuilder.build();
    }

    private String decode(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }
}

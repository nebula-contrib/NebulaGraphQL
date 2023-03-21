package com.nebulagraphql.schema;

import com.nebulagraphql.util.SchemaUtils;
import com.vesoft.nebula.PropertyType;
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

import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class SchemaManger {
    private final MetaClient metaClient;

    private static final Map<String, Map<String, Map<String, PropertyType>>> spaceTagsFieldsMap = new HashMap<>();

    public SchemaManger(List<HostAddress> addresses) throws UnknownHostException {
        this.metaClient = new MetaClient(addresses,30000,3,3);
    }

    public static Map<String, Map<String, Map<String, PropertyType>>> getSpaceTagsFieldsMap() {
        return spaceTagsFieldsMap;
    }

    public GraphQLSchema generateSchema(String space) {
        try {
            metaClient.connect();
            List<TagItem> tags = metaClient.getTags(space);
            GraphQLObjectType.Builder queryTypeBuilder = GraphQLObjectType.newObject();
            GraphQLCodeRegistry.Builder codeRegistryBuilder = GraphQLCodeRegistry.newCodeRegistry();
            DataFetcher<Object> propertyDataFetcher = new NebulaDataFetcher();
            queryTypeBuilder.name("Query");
            for (TagItem tag : tags) {
                GraphQLObjectType.Builder tagTypeBuilder = GraphQLObjectType.newObject();
                String tagName = new String(tag.getTag_name(), StandardCharsets.UTF_8);
                tagTypeBuilder.name(tagName);
                Schema schema = tag.getSchema();
                List<GraphQLArgument> arguments = new ArrayList<>();
                Map<String, Map<String, PropertyType>> tagsFieldsMap = new HashMap<>();
                Map<String, PropertyType> fieldsMap = new HashMap<>();
                for (ColumnDef columnDef : schema.getColumns()) {
                    GraphQLFieldDefinition.Builder fieldDefinitionBuilder = GraphQLFieldDefinition.newFieldDefinition();
                    String fieldName = new String(columnDef.getName(), StandardCharsets.UTF_8);
                    GraphQLScalarType scalarType = SchemaUtils.getType(columnDef.type.getType());
                    fieldsMap.put(fieldName, columnDef.type.getType());
                    GraphQLArgument.Builder argumentBuilder = GraphQLArgument.newArgument();
                    argumentBuilder.name(fieldName).type(scalarType).defaultValueLiteral(NullValue.of());
                    byte[] desc = columnDef.getComment();
                    if(desc!=null){
                        argumentBuilder.description(new String(desc,StandardCharsets.UTF_8));
                    }
                    GraphQLArgument argument = argumentBuilder.build();
                    arguments.add(argument);

                    fieldDefinitionBuilder.name(fieldName)
                            .type(scalarType);
                    if(desc!=null){
                        fieldDefinitionBuilder.description(new String(desc,StandardCharsets.UTF_8));
                    }
                    tagTypeBuilder.field(fieldDefinitionBuilder);
                }
                tagsFieldsMap.put(tagName, fieldsMap);
                spaceTagsFieldsMap.put(space, tagsFieldsMap);
                GraphQLObjectType tagType = tagTypeBuilder.build();

                //add query for vertices according to properties
                queryTypeBuilder.field(GraphQLFieldDefinition.newFieldDefinition()
                        .name(tagName + "s")
                        .type(GraphQLNonNull.nonNull(GraphQLList.list(tagType)))// if there is no matching vertex, return empty list
                        .arguments(arguments)
                        .build());
                //add query for specific vertex according to VID
                queryTypeBuilder.field(GraphQLFieldDefinition.newFieldDefinition()
                        .name(tagName)
                        .type(tagType)// result will be null if VID not exist
                        .argument(GraphQLArgument.newArgument()
                                .name("ID")
                                .type(Scalars.GraphQLID)
                                .description("Vertex ID")
                                .build())
                        .build());
                codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates("Query", tagName + "s"), propertyDataFetcher);
            }
            GraphQLCodeRegistry codeRegistry = codeRegistryBuilder.build();
            GraphQLObjectType queryType = queryTypeBuilder.build();
            GraphQLSchema graphQLSchema = GraphQLSchema.newSchema()
                    .query(queryType)
                    .codeRegistry(codeRegistry)
                    .build();
            return graphQLSchema;
        } catch (ClientServerIncompatibleException e) {
            throw new RuntimeException(e);
        } catch (ExecuteFailedException e) {
            throw new RuntimeException(e);
        } finally {
            metaClient.close();
        }
    }
}

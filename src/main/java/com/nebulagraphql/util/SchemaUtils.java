package com.nebulagraphql.util;

import com.vesoft.nebula.PropertyType;
import graphql.Scalars;
import graphql.schema.GraphQLScalarType;

public class SchemaUtils {
    public static GraphQLScalarType getType(PropertyType propertyType) {
        switch (propertyType) {
            case BOOL:
                return Scalars.GraphQLBoolean;
            case VID:
                return Scalars.GraphQLID;
            case INT8:
            case INT16:
            case INT32:
            case INT64:
            case TIMESTAMP:
            case TIME:
            case DATE:
            case DATETIME:
            case DURATION:
            case GEOGRAPHY:
                return Scalars.GraphQLInt;
            case FLOAT:
            case DOUBLE:
                return Scalars.GraphQLFloat;
            case STRING:
            case FIXED_STRING:
                return Scalars.GraphQLString;
            default:
                throw new UnsupportedOperationException();
        }
    }
}

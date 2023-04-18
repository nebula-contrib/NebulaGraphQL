package com.nebulagraphql.util;

import com.nebulagraphql.session.MetaData;
import com.vesoft.nebula.PropertyType;

import java.util.Map;

public class NgqlUtils {
    private static final String QUOTE_PATTERN = "\"%s\"";

    public static String process(MetaData metaData, String tag, String field, String value) {
        Map<String, PropertyType> tagSchema = metaData.getTagPropertyTypes(tag);
        PropertyType propertyType = tagSchema.get(field);
        if (propertyType == null) {
            throw new RuntimeException();
        }
        //TODO support more propertyTYpe
        switch (propertyType) {
            case STRING:
            case FIXED_STRING:
            case VID:
                return String.format(QUOTE_PATTERN, value);
            default:
                return value;
        }
    }
}

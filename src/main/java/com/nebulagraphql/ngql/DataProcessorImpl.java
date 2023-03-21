package com.nebulagraphql.ngql;

import com.nebulagraphql.schema.SchemaManger;
import com.vesoft.nebula.PropertyType;

import java.util.HashMap;
import java.util.Map;

public class DataProcessorImpl implements DataProcessor{
    private final String space;

    private final String tag;

    private static final String QUOTE_PATTERN = "\"%s\"";

    public DataProcessorImpl(String space,String tag){
        this.space = space;
        this.tag = tag;
    }

    @Override
    public String process(String field, String value) {
        Map<String,PropertyType> lookUp = SchemaManger.getSpaceTagsFieldsMap()
                .getOrDefault(space,new HashMap<>())
                .getOrDefault(tag,new HashMap<>());
        PropertyType propertyType = lookUp.get(field);
        if(propertyType==null){
            throw new RuntimeException();
        }
        //TODO support more propertyTYpe
        switch (propertyType){
            case STRING:
            case FIXED_STRING:
            case VID:
                return String.format(QUOTE_PATTERN, value);
            default:
                return value;
        }
    }
}

package com.nebulagraphql.ngql;

import com.nebulagraphql.schema.SchemaManger;
import com.vesoft.nebula.PropertyType;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataProcessorImpl implements DataProcessor{
    private static final Logger logger = LoggerFactory.getLogger(DataProcessorImpl.class);

    private final String space;

    private final String tag;

    private static final String QUOTE_PATTERN = "\"%s\"";

    public DataProcessorImpl(String space,String tag){
        this.space = space;
        this.tag = tag;
    }

    @Override
    public String process(String field, String value) {
        logger.debug("processing, key:{}, value:{}",field,value);
        Map<String,PropertyType> lookUp = SchemaManger.getSpaceTagsFieldsMap()
                .getOrDefault(space,new HashMap<>())
                .getOrDefault(tag,new HashMap<>());
        logger.debug("lookUp table: {}",lookUp);
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

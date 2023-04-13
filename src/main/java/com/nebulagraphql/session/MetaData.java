package com.nebulagraphql.session;

import java.util.Map;

import com.vesoft.nebula.PropertyType;

public class MetaData {
    private Map<String,Map<String,PropertyType>> tagSchema;

    

    public MetaData(Map<String, Map<String, PropertyType>> tagSchema) {
        this.tagSchema = tagSchema;
    }

    public Map<String, Map<String, PropertyType>> getTagSchema() {
        return tagSchema;
    }

    public void setTagSchema(Map<String, Map<String, PropertyType>> tagSchema) {
        this.tagSchema = tagSchema;
    }

    public Map<String,PropertyType> getTagPropertyTypes(String tag){
        return tagSchema.get(tag);
    }

}

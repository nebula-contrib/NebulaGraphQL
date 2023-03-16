package com.nebulagraphql.rsboot.domain;

import java.util.HashMap;
import java.util.Map;

public class Tag {
    private final String name;
    private final Map<String, Object> properties;

    private Tag(String name) {
        this.name = name;
        this.properties = new HashMap<>();
    }

    public static Tag setName(String name) {
        return new Tag(name);
    }

    public Tag setProperty(String field, Object name) {
        this.properties.put(field, name);
        return this;
    }

    public String getName() {
        return name;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    @Override
    public String toString() {
        return "Tag{" +
                "name='" + name + '\'' +
                ", properties=" + properties +
                '}';
    }
}

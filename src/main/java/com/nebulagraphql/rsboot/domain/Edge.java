package com.nebulagraphql.rsboot.domain;

import java.util.HashMap;
import java.util.Map;

public class Edge {
    private final String src;
    private final String dst;
    private final String name;
    private final Long ranking;
    private final Map<String, Object> properties;

    private Edge(String src, String dst, String name, Long ranking) {
        this.src = src;
        this.dst = dst;
        this.name = name;
        this.ranking = ranking;
        this.properties = new HashMap<>();
    }

    public static Edge preFabricate(String src, String dst, String name, Long ranking) {
        return new Edge(src, dst, name, ranking);
    }

    public Edge setProperty(String field, Object value) {
        this.properties.put(field, value);
        return this;
    }

    public String getSrc() {
        return src;
    }

    public String getDst() {
        return dst;
    }

    public String getName() {
        return name;
    }

    public Long getRanking() {
        return ranking;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    @Override
    public String toString() {
        return "Edge{" +
                "src='" + src + '\'' +
                ", dst='" + dst + '\'' +
                ", name='" + name + '\'' +
                ", ranking=" + ranking +
                ", properties=" + properties +
                '}';
    }
}

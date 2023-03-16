package com.nebulagraphql.rsboot.parser;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nebulagraphql.rsboot.domain.*;
import com.vesoft.nebula.client.graph.data.Node;
import com.vesoft.nebula.client.graph.data.PathWrapper;
import com.vesoft.nebula.client.graph.data.Relationship;
import com.vesoft.nebula.client.graph.data.ValueWrapper;

import java.io.UnsupportedEncodingException;
import java.util.*;

public class BasicParser extends BaseParser {
    public BasicParser(ValueWrapper valueWrapper) {
        super(valueWrapper);
    }

    @Override
    public Object parse(){
        Object v = null;
        try{
            if (valueWrapper.isString()) {
                v = valueWrapper.asString();
            } else if (valueWrapper.isLong()) {
                v = valueWrapper.asLong();
            } else if (valueWrapper.isBoolean()) {
                v = valueWrapper.asBoolean();
            } else if (valueWrapper.isDouble()) {
                v = valueWrapper.asDouble();
            } else if (valueWrapper.isTime()) {
                v = valueWrapper.asDouble();
            } else if (valueWrapper.isDate()) {
                v = valueWrapper.asDate().toString();
            } else if (valueWrapper.isDateTime()) {
                v = valueWrapper.asDateTime();
            } else if (valueWrapper.isGeography()) {
                v = valueWrapper.asGeography().toString();
            } else if (valueWrapper.isDuration()){
                v = valueWrapper.asDuration().toString();
            } else if (valueWrapper.isSet()) {
                v = defaultSetParser(valueWrapper);
            } else if (valueWrapper.isList()) {
                v = defaultListParser(valueWrapper);
            } else if (valueWrapper.isMap()) {
                v = defaultMapParser(valueWrapper);
            } else if (valueWrapper.isVertex()) {
                v = defaultVertexParser(valueWrapper.asNode());
            } else if (valueWrapper.isEdge()) {
                v = defaultEdgeParser(valueWrapper.asRelationship());
            } else if (valueWrapper.isPath()) {
                v = defaultPathPaser(valueWrapper);
            }
        }catch (UnsupportedEncodingException e){
            throw new ParserException(e);
        }
        return v;
    }

    private Object defaultListParser(ValueWrapper valueWrapper) {
        List<ValueWrapper> originList = valueWrapper.asList();
        List<Object> resList = new ArrayList<>();
        for (ValueWrapper e : originList) {
            resList.add(ParserFactory.parse(e));
        }
        return resList;
    }

    private Object defaultSetParser(ValueWrapper valueWrapper) {
        Set<ValueWrapper> originList = valueWrapper.asSet();
        Set<Object> resList = new HashSet<>();
        for (ValueWrapper e : originList) {
            resList.add(ParserFactory.parse(e));
        }
        return resList;
    }

    private Object defaultMapParser(ValueWrapper valueWrapper) throws UnsupportedEncodingException {
        Map<String, ValueWrapper> originMap = valueWrapper.asMap();
        Map<String, Object> resMap = new HashMap<>();
        for (String key : originMap.keySet()) {
            resMap.put(key, ParserFactory.parse(originMap.get(key)));
        }
        return resMap;
    }

    private Object defaultVertexParser(Node node) throws UnsupportedEncodingException {
        String id = ParserFactory.getParser(node.getId()).parse().toString();
        Vertex vertex = Vertex.setId(id);
        for (String tagName : node.tagNames()) {
            Tag tag = Tag.setName(tagName);
            Map<String, ValueWrapper> properties = node.properties(tagName);
            for (Map.Entry<String, ValueWrapper> property : properties.entrySet()) {
                String field = property.getKey();
                Object value = ParserFactory.getParser(property.getValue()).parse();
                tag.setProperty(field, value);
            }
            vertex.addTag(tag);
        }
        return vertex;
    }

    private Object defaultEdgeParser(Relationship relationship) throws UnsupportedEncodingException {
        String srcId = ParserFactory.getParser(relationship.srcId()).parse().toString();
        String dstId = ParserFactory.getParser(relationship.dstId()).parse().toString();
        String name = relationship.edgeName();
        Long ranking = relationship.ranking();
        Edge edge = Edge.preFabricate(srcId, dstId, name, ranking);
        Map<String, ValueWrapper> properties = relationship.properties();
        for (Map.Entry<String, ValueWrapper> property : properties.entrySet()) {
            String field = property.getKey();
            Object value = ParserFactory.getParser(property.getValue()).parse();
            edge.setProperty(field, value);
        }
        return edge;
    }

    private Object defaultPathPaser(ValueWrapper valueWrapper) throws UnsupportedEncodingException {
        PathWrapper pathWrapper = valueWrapper.asPath();
        List<PathWrapper.Segment> segments = pathWrapper.getSegments();
        List<Segment> domainSegments = new ArrayList<>();
        List<Vertex> nodes = new ArrayList<>();
        if (segments.isEmpty()) {
            for (Node node : pathWrapper.getNodes()) {
                nodes.add((Vertex) defaultVertexParser(node));
            }
        } else {
            for (PathWrapper.Segment segment : segments) {
                Vertex src = (Vertex) defaultVertexParser(segment.getStartNode());
                Vertex dst = (Vertex) defaultVertexParser(segment.getEndNode());
                Edge edge = (Edge) defaultEdgeParser(segment.getRelationShip());
                domainSegments.add(new Segment(src, dst, edge));
            }
        }
        if (!nodes.isEmpty()) {
            return nodes;
        }
        return new Path(domainSegments);
    }

    private JSONArray getNodeInfo(Node node) throws UnsupportedEncodingException {
        List<String> tagNames = node.tagNames();
        JSONArray res = new JSONArray();
        for (String tagName : tagNames) {
            Object vertexId = ParserFactory.parse(node.getId());
            JSONObject vertexInfo = new JSONObject();
            vertexInfo.put("id", vertexId);
            vertexInfo.put("label", tagName);
            Map<String, ValueWrapper> originMap = node.properties(tagName);
            Map<String, Object> properties = new HashMap<>();
            for (String key : originMap.keySet()) {
                properties.put(key, ParserFactory.parse(originMap.get(key)));
            }
            vertexInfo.put("properties", properties);
            res.add(vertexInfo);
        }
        return res;
    }

}

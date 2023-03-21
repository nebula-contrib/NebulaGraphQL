package com.nebulagraphql.ngql;

import java.util.Iterator;
import java.util.Map;

public class GetVerticesByProperty implements GeneralQuery {
    private final String tagName;
    private final Map<String, String> properties;

    private final StringBuilder statement;

    public GetVerticesByProperty(String tagName, Map<String, String> properties) {
        this.tagName = tagName;
        this.properties = properties;
        this.statement = new StringBuilder("LOOKUP ON ");
    }

    @Override
    public String toQuery() {
        statement.append(tagName).append(" ").append("WHERE");
        Iterator<Map.Entry<String, String>> iterator = properties.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> property = iterator.next();
            statement.append(" ").append(tagName).append(".").append(property.getKey())
                    .append(" == ").append(property.getValue());
            if (iterator.hasNext()) {
                statement.append(" AND");
            }
        }
        statement.append(" YIELD id(vertex) as vertexId | FETCH PROP ON ").append(tagName)
                .append(" $-.vertexId YIELD vertex AS v;");
        return statement.toString();
    }
}

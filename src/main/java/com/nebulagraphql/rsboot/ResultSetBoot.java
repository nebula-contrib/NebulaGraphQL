package com.nebulagraphql.rsboot;

import com.alibaba.fastjson.JSONObject;
import com.nebulagraphql.rsboot.domain.Vertex;
import com.nebulagraphql.rsboot.parser.ParserFactory;
import com.vesoft.nebula.client.graph.data.ResultSet;
import com.vesoft.nebula.client.graph.data.ValueWrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ResultSetBoot {
    private final ResultSet resultSet;
    private boolean withColumnName;
    private boolean matrixStyle;
    private boolean rowOriented;

    private ResultSetBoot(ResultSet resultSet) {
        this.resultSet = resultSet;
        this.withColumnName = false;
        this.matrixStyle = false;
    }

    public static ResultSetBoot wrap(ResultSet resultSet) {
        return new ResultSetBoot(resultSet);
    }

    public ResultSetBoot withColumnName() {
        this.withColumnName = true;
        return this;
    }

    public ResultSetBoot useMatrixStyle() {
        this.matrixStyle = true;
        return this;
    }

    public ResultSetBoot rowOriented() {
        this.rowOriented = true;
        return this;
    }

    public List<Vertex> getVertices() {
        List<String> columNames = resultSet.getColumnNames();
        List<Vertex> vertices = new ArrayList<>();
        for(String columName:columNames){
            for(ValueWrapper valueWrapper: resultSet.colValues(columName)){
                if(valueWrapper.isVertex()){
                    Vertex vertex = (Vertex) ParserFactory.getParser(valueWrapper).parse();
                    vertices.add(vertex);
                }
            }
        }
        return vertices;
    }

    public Object toJson(){
        if (!resultSet.isSucceeded()) {
            JSONObject failure = new JSONObject();
            failure.put("error_code", resultSet.getErrorCode());
            failure.put("error_message", resultSet.getErrorMessage());
            return failure;
        }
        List<List<Object>> matrix = new ArrayList<>();
        List<String> columnNames = resultSet.getColumnNames();
        for (String colName : columnNames) {
            List<Object> col = new ArrayList<>();
            for (ValueWrapper valueWrapper : resultSet.colValues(colName)) {
                Object cell = ParserFactory.getParser(valueWrapper).parse();
                col.add(cell);
            }
            matrix.add(col);
        }
        if (rowOriented) {
            List<List<Object>> transposedMatrix = new ArrayList<>();
            int col = matrix.get(0).size();
            for (int i = 0; i < col; i++) {
                List<Object> transposedRow = new ArrayList<>();
                for (List<Object> row : matrix) {
                    transposedRow.add(row.get(i));
                }
                transposedMatrix.add(transposedRow);
            }
            if (withColumnName) {
                List<Map<String, Object>> matrixWithName = new ArrayList<>();
                for (List<Object> row : transposedMatrix) {
                    Map<String, Object> newRow = new HashMap<>();
                    for (int i = 0; i < columnNames.size(); i++) {
                        newRow.put(columnNames.get(i), row.get(i));
                    }
                    matrixWithName.add(newRow);
                }
                return JSONObject.toJSON(matrixWithName);
            }
        }
        if (withColumnName) {
            Map<String, List<Object>> matrixWithName = new HashMap<>();
            for (int i = 0; i < columnNames.size(); i++) {
                matrixWithName.put(columnNames.get(i), matrix.get(i));
            }
            return JSONObject.toJSON(matrixWithName);
        } else if (matrixStyle) {
            return JSONObject.toJSON(matrix);
        } else {
            return JSONObject.toJSON(matrix.stream().flatMap(List::stream).collect(Collectors.toList()));
        }
    }

}

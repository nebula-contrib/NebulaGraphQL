package com.nebulagraphql.ngql;

/**
 * DataProcessor will use schema from
 */
public interface DataProcessor {
    /**
     * process value according field
     * @param field
     * @param value
     * @return
     */
    String process(String field,String value);
}

package com.nebulagraphql.rsboot.parser;

import com.vesoft.nebula.client.graph.data.ValueWrapper;

public abstract class BaseParser implements Parser {
    protected ValueWrapper valueWrapper;

    public BaseParser(ValueWrapper valueWrapper) {
        this.valueWrapper = valueWrapper;
    }
}

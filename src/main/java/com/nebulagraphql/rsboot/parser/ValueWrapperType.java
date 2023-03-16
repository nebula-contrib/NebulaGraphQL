package com.nebulagraphql.rsboot.parser;

public enum ValueWrapperType {
    NULL(1),
    BOOLEAN(2),
    INT(3),
    FLOAT(4),
    STRING(5),
    DATE(6),
    TIME(7),
    DATETIME(8),
    VERTEX(9),
    EDGE(10),
    PATH(11),
    LIST(12),
    MAP(13),
    SET(14),
    DATASET(15),
    GEOGRAPHY(16),
    DURATION(17);

    private final int value;

    ValueWrapperType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

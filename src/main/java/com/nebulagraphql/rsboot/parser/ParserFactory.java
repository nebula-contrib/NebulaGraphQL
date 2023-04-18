package com.nebulagraphql.rsboot.parser;

import com.vesoft.nebula.client.graph.data.ValueWrapper;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class ParserFactory {
    private static final Map<Integer, Class<?>> PARSER_MAP = new HashMap<>();

    private ParserFactory() {

    }

    public static Parser getParser(ValueWrapper valueWrapper) {
        Class<?> clazz = PARSER_MAP.getOrDefault(valueWrapper.getValue().getSetField(), BasicParser.class);
        Constructor constructor;
        try {
            constructor = clazz.getDeclaredConstructor(ValueWrapper.class);
        } catch (NoSuchMethodException e) {
            throw new ParserException(e);
        }
        Parser parser;
        try {
            parser = (Parser) constructor.newInstance(valueWrapper);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new ParserException(e);
        }
        return parser;
    }

    public static <T extends Parser> void registerParser(ValueWrapperType valueWrapperType, Class<T> clazz) {
        PARSER_MAP.put(valueWrapperType.getValue(), clazz);
    }

    public static Object parse(ValueWrapper valueWrapper) {
        return getParser(valueWrapper).parse();
    }
}

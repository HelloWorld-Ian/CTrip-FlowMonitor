package com.ctrip.ibu.flow.monitor.service.tool.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

import java.io.IOException;
import java.util.List;

/**
 *
 * 使用新版com.fasterxml.jackson.databind.ObjectMapper，
 * 而不是org.codehaus.jackson.map.ObjectMapper
 *
 * json工具类
 *
 * @author Iancy
 * @date 2022/1/17
 */
public class JsonUtils {

    private static final ObjectMapper mapper=new ObjectMapper();

    public static JsonNode readTree(String jsonStr) {
        try {
            return mapper.readTree(jsonStr);
        } catch (IOException e) {
            return null;
        }
    }

    public static <T> List<T> readValues(String jsonStr, Class<T> clazz) {
        CollectionType javaType = mapper.getTypeFactory().constructCollectionType(List.class, clazz);
        try {
            return mapper.readValue(jsonStr, javaType);
        } catch (IOException e) {
            return null;
        }
    }

    public static String toJson(Object target){
        try {
            return mapper.writeValueAsString(target);
        } catch (IOException e) {
            return null;
        }
    }

    public static <T> T readValue(String jsonStr, Class<T>type){
        try {
            return mapper.readValue(jsonStr,type);
        } catch (IOException e) {
            return null;
        }
    }
}

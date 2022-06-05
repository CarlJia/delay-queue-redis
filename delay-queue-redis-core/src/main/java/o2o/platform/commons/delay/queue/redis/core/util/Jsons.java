package o2o.platform.commons.delay.queue.redis.core.util;

import java.io.IOException;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * @author zhouyang01
 * Created on 2022-06-05
 */
@SuppressWarnings("unused")
public class Jsons {
    private Jsons() {
    }


    public static final String EMPTY_JSON = "{}";
    private static final ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        //        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);       //属性为NULL 不序列化
        //        mapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);    //属性为默认值不序列化
        //        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);      // 属性为 空（“”） 或者为 NULL 都不序列化
        mapper.configure(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED, false);//序列化：:
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); //反序列化出现未知属性忽略
    }


    @Nonnull
    public static <T> String toJson(T obj) {
        if (obj == null) {
            return EMPTY_JSON;
        }

        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("object to json error", e);
        }
    }

    @Nonnull
    public static <T> String toJsonPretty(T obj) {
        if (obj == null) {
            return EMPTY_JSON;
        }

        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("object to json error", e);
        }
    }

    @Nonnull
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (json == null) {
            throw new RuntimeException("json to object error, jsonString is null");
        }

        try {
            return mapper.readValue(json, clazz);
        } catch (IOException e) {
            throw new RuntimeException("json to object error", e);
        }
    }

    /**
     * 可以通过 {@code createCollectionType(Map.class, Long.class, String.class)}
     */
    @Nonnull
    public static <T> T fromJson(String json, JavaType javaType) {
        if (json == null) {
            throw new RuntimeException("json to object error, jsonString is null");
        }

        try {
            return mapper.readValue(json, javaType);
        } catch (IOException e) {
            throw new RuntimeException("json to object error", e);
        }
    }

    /**
     * 可以通过 {@code new TypeReference<Map<String,List<Bean>>>(){}}来调用
     */
    @Nonnull
    public static <T> T fromJson(String json, TypeReference<T> tr) {
        if (json == null) {
            throw new RuntimeException("json to object error, jsonString is null");
        }

        try {
            return mapper.readValue(json, tr);
        } catch (IOException e) {
            throw new RuntimeException("json to object error", e);
        }
    }

    public static JavaType createCollectionType(Class<?> collectionClass, Class<?>... elementClasses) {
        return mapper.getTypeFactory().constructParametricType(collectionClass, elementClasses);
    }
}


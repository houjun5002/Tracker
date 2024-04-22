package com.zsmarter.exposuretracker.util;

import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

/**
 * 类描述：项目的util工具 gson解析
 */
public class GsonUtils {
    public static Gson gson = new Gson();

    /**
     * 说明：如果解析抛异常返回null
     *
     * @param result 要解析的json字符串
     * @param clazz  对应的javabean的字节码
     * @return 返回 对应的javabean 对象
     */
    public static <T> T fromJson(String result, Class<T> clazz) {
        try {
            if (gson == null) {
                gson = new Gson();
            }
            return gson.fromJson(result, clazz);
        } catch (Exception e) {
            return null;
        }
    }

    public static <T> T fromJson(String json, Type typeOfT) {
        try {
            if (gson == null) {
                gson = new Gson();
            }
            return gson.fromJson(json, typeOfT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String toJson(Object obj) {
        if (null == gson) {
            gson = new Gson();
            // gson =   new GsonBuilder().disableHtmlEscaping().create();
        }
        return gson.toJson(obj);
    }

    public static <T> List<T> jsonStringConvertToList(String string, Class<T[]> cls) {
        Gson gson = new Gson();
        T[] array = gson.fromJson(string, cls);
        return Arrays.asList(array);
    }

}

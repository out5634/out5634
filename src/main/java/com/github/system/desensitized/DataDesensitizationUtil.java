package com.github.system.desensitized;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.DesensitizedUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 脱敏工具类
 *
 * @author oldwu
 */
@Slf4j
public class DataDesensitizationUtil {

    public static Object desensitizationValue(DataDesensitization dataDesensitization, Object fieldValue) {
        String string = StrUtil.toString(fieldValue);
        if (StrUtil.isNotBlank(string)) {
            if (fieldValue instanceof String str) {
                switch (dataDesensitization.value()) {
                    case NORMAL -> {
                        if (str.length() <= 2) {
                            return StrUtil.hide(str, 0, str.length());
                        } else if (NumberUtil.isNumber(str)) {
                            return DesensitizedUtil.desensitized(str, DesensitizedUtil.DesensitizedType.USER_ID);
                        } else {
                            return normal(str);
                        }
                    }
                    case MOBILE_PHONE -> {
                        return DesensitizedUtil.mobilePhone(str);
                    }
                    case EMAIL -> {
                        return DesensitizedUtil.email(str);
                    }
                    case PASSWORD -> {
                        return "**********";
                    }
                    case USER_ID -> {
                        return DesensitizedUtil.desensitized(str, DesensitizedUtil.DesensitizedType.USER_ID);
                    }
                    case USER_NAME -> {
                        return normal(str);
                    }
                    case BLANK -> {
                        return "";
                    }
                    case NULL -> {
                        return null;
                    }
                }
            } else if (fieldValue instanceof Integer i) {
                return normalInt(i);
            } else if (fieldValue instanceof Long l) {
                return normalLong(l);
            } else if (fieldValue instanceof List<?> list) {
                if (!list.isEmpty()) {
                    // 获取list的泛型，正常人应该不会写List<Object>这种
                    Class<?> aClass = list.get(0).getClass();
                    // 递归脱敏list中的内容
                    return list(dataDesensitization, list, aClass);
                }
            } else if (fieldValue instanceof Map<?, ?> map) {
                ParameterizedType parameterizedType = (ParameterizedType) map.getClass().getGenericSuperclass();
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                return map(dataDesensitization, map, (Class<?>) actualTypeArguments[0], (Class<?>) actualTypeArguments[1]);
            } else {
                log.error("不能识别的脱敏字段类型:{},class={},已填充NULL", fieldValue.getClass().getName(), fieldValue.getClass().getName());
                return null;
            }
        }
        return null;
    }

    public static <K, V> Map<K, V> map(DataDesensitization dataDesensitization, Map<?, ?> inputMap, Class<K> mapKeyClass, Class<V> mapValueClass) {
        Map<K, V> map = MapUtil.createMap(inputMap.getClass());
        inputMap.forEach((k, v) -> map.put((K) k, (V) desensitizationValue(dataDesensitization, v)));
        return map;
    }

    public static <T> List<T> list(DataDesensitization dataDesensitization, List<?> inputList, Class<T> clazz) {
        List<T> resultList = new ArrayList<>();
        for (Object object : inputList) {
            T o = (T) desensitizationValue(dataDesensitization, object);
            resultList.add(o);
        }
        return resultList;
    }

    public static String normal(String str) {
        if (StrUtil.length(str) >= 3) {
            return StrUtil.hide(str, 1, StrUtil.length(str) - 1);
        } else {
            return StrUtil.hide(str, 0, StrUtil.length(str));
        }
    }

    public static Long normalLong(Long l) {
        String str = StrUtil.toString(l);
        if (StrUtil.length(str) >= 3) {
            return Long.parseLong(StrUtil.replace(str, 1, StrUtil.length(str) - 1, "0"));
        } else {
            return Long.parseLong(StrUtil.replace(str, 0, StrUtil.length(str), "0"));
        }
    }

    public static Integer normalInt(Integer i) {
        String str = StrUtil.toString(i);
        if (StrUtil.length(str) >= 3) {
            return Integer.parseInt(StrUtil.replace(str, 1, StrUtil.length(str) - 1, "0"));
        } else {
            return Integer.parseInt(StrUtil.replace(str, 0, StrUtil.length(str), "0"));
        }
    }


    public static <T> void desensitization(T t) {
        Field[] fields = ReflectUtil.getFields(t.getClass());
        for (Field field : fields) {
            DataDesensitization annotation = AnnotationUtil.getAnnotation(field, DataDesensitization.class);
            if (annotation != null) {
                // 启用数据脱敏
                Object fieldValue = BeanUtil.getFieldValue(t, field.getName());
                BeanUtil.setFieldValue(t, field.getName(), desensitizationValue(annotation, fieldValue));
            }
        }
    }

}

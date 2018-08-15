package com.php25.common.core.specification;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

public class BaseSpecsFactory {

    private static final Logger log = LoggerFactory.getLogger(BaseSpecsFactory.class);

    private static ConcurrentHashMap<String, BaseSpecs> concurrentHashMap = new ConcurrentHashMap<>();

    public static <T> BaseSpecs<T> getInstance(Class<? extends BaseSpecs> cls) {
        try {
            BaseSpecs result = concurrentHashMap.get(cls.getName());
            if (null == result) {
                result = cls.newInstance();
                concurrentHashMap.put(cls.getName(), result);
            }
            return (BaseSpecs<T>) result;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("获取BaseSpecs实例失败", e);
        }
    }

}

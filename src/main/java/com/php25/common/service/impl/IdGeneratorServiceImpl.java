package com.php25.common.service.impl;


import com.php25.common.service.IdGeneratorService;
import com.php25.common.service.RedisService;
import com.php25.common.specification.SnowflakeIdWorker;
import com.php25.common.util.RandomUtil;
import com.php25.common.util.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.UUID;

/**
 * Created by penghuiping on 2017/9/18.
 */
@Service("idGeneratorService")
public class IdGeneratorServiceImpl implements IdGeneratorService {

    @Autowired
    private SnowflakeIdWorker snowflakeIdWorker;

    @Autowired
    private RedisService redisService;

    @Override
    public String getVipOrderNumber() {
        return TimeUtil.getTime(new Date(), DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + RandomUtil.getRandomNumbers(6);
    }

    @Override
    public String getModelPrimaryKey() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    @Override
    public Number getModelPrimaryKeyNumber() {
        return snowflakeIdWorker.nextId();
    }
}

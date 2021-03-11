package com.php25.common.mq.redis;

import com.php25.common.core.util.StringUtil;
import com.php25.common.mq.Message;
import com.php25.common.redis.RHash;
import com.php25.common.redis.RList;
import com.php25.common.redis.RSet;
import com.php25.common.redis.RedisManager;

/**
 * @author penghuiping
 * @date 2021/3/11 10:14
 */
class RedisQueueGroupFinder {

    private final RedisManager redisManager;

    public RedisQueueGroupFinder(RedisManager redisManager) {
        this.redisManager = redisManager;
    }

    /**
     * 获取系统中所有队列名
     *
     * @return 队列名
     */
    public RSet<String> queues() {
        return this.redisManager.set(RedisConstant.QUEUES, String.class);
    }

    /**
     * 根据队列名获取队列
     *
     * @param queue 队列名
     * @return 队列
     */
    public RList<Message> queue(String queue) {
        return this.redisManager.list(RedisConstant.QUEUE_PREFIX + queue, Message.class);
    }

    /**
     * 根据组名获取组
     *
     * @param group 组名
     * @return 组
     */
    public RList<Message> group(String group) {
        return this.redisManager.list(RedisConstant.GROUP_PREFIX + group, Message.class);
    }

    /**
     * 根据队列名获取绑定的组名
     *
     * @param queue 队列名
     * @return 队列与组关系
     */
    public RSet<String> groups(String queue) {
        return this.redisManager.set(RedisConstant.QUEUE_GROUPS_PREFIX + queue, String.class);
    }

    /**
     * 获取系统中的消息缓存
     *
     * @return 消息缓存
     */
    public RHash<RedisMessage> messagesCache() {
        return this.redisManager.hash(RedisConstant.MESSAGES_CACHE, RedisMessage.class);
    }

    /**
     * 根据队列名获取死信队列
     * 1. 如果没有绑定死信队列,
     *
     * @param queue 队列名
     * @return 死信队列
     */
    public RList<Message> dlq(String queue) {
        String dlq = this.redisManager.string().get(RedisConstant.QUEUE_DLQ_PREFIX + queue, String.class);
        if (StringUtil.isBlank(dlq)) {
            return this.redisManager.list(RedisConstant.DLQ_DEFAULT, Message.class);
        }
        return this.redisManager.list(RedisConstant.DLQ_PREFIX + dlq, Message.class);
    }
}

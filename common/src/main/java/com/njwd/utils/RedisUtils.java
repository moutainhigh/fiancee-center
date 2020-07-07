
package com.njwd.utils;


import com.njwd.common.Constant;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.*;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisCommands;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * redis 工具类
 *
 * @author CJ
 */
@Component
public class RedisUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisUtils.class);
    /**
     * 使用lua脚本删除redis中匹配value的key,避免由于方法执行时间过长而redis锁自动过期失效时误删其他线程的锁
     */
    private static final String UNLOCK_LUA = "if redis.call('get',KEYS[1]) == ARGV[1] " +
            "then " +
            "    return redis.call('del',KEYS[1]) " +
            "else " +
            "    return 0 " +
            "end ";

    private static final String MULTI_LOCK_SCRIPT = "" +
            "for k,v in pairs(KEYS) do " +
            "    if (redis.call('exists',v) == 0 or redis.call('get',v) == ARGV[1]) then " +
            "        if(not redis.call('setex',v,tonumber(ARGV[2]),ARGV[1])) then" +
            "            return 0 " +
            "        end " +
            "    else " +
            "        return 0 " +
            "    end " +
            "end " +
            "return 1 ";

    private static final String MULTI_UNLOCK_SCRIPT = "" +
            "local count = 0 " +
            "for k, v in pairs(KEYS) do " +
            "    if (redis.call('get',v) == ARGV[1]) then " +
            "        if(redis.call('del',v)) then " +
            "            count = count + 1 " +
            "        end " +
            "    end " +
            "end " +
            "return count ";

    public static RedisTemplate<String, String> CLIENT;
    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @PostConstruct
    private void init() {
        CLIENT = redisTemplate;
    }

    /**
     * 设置缓存数据
     *
     * @param key   键
     * @param value 值
     */
    public static void set(String key, String value) {
        ValueOperations<String, String> valueOps = CLIENT.opsForValue();
        valueOps.set(key, value);
    }

    public static void set(String key, String value, long timeout, TimeUnit unit) {
        ValueOperations<String, String> valueOps = CLIENT.opsForValue();
        valueOps.set(key, value, timeout, unit);
    }

    public static void expire(String key, long timeout, TimeUnit unit) {
        CLIENT.expire(key, timeout, unit);
    }

    /**
     * 获取缓存数据
     *
     * @param key 键
     */
    public static String get(String key) {
        ValueOperations<String, String> valueOps = CLIENT.opsForValue();
        return valueOps.get(key);
    }


    /**
     * 删除key
     */
    public static void remove(String key) {
        CLIENT.delete(key);
    }

    /**
     * 模糊删除key
     *
     * @param pattern  如: dictTree:sysConfig:*
     * @param countNum 每次扫描的key个数,默认10
     */
    public static void removeKeys(String pattern, @Nullable Long countNum) {
        ScanOptions.ScanOptionsBuilder match = ScanOptions.scanOptions().match(pattern);
        if (countNum != null) {
            match.count(countNum);
        }
        Cursor<byte[]> scan = Objects.requireNonNull(CLIENT.getConnectionFactory()).getConnection().scan(match.build());
        List<String> keys = new LinkedList<>();
        while (scan.hasNext()) {
            byte[] next = scan.next();
            keys.add(new String(next));
        }
        if (keys.size() > 0) {
            CLIENT.delete(keys);
        }
    }

    /**
     * @description 删除单个缓存对象 key为String
     * @author fancl
     * @date 2019/8/28
     * @param collect 集合的名字 key：键值 一般为id 或者为拼接的key
     * @return
     */
    public static void remove(String collect, String key) {
        CLIENT.delete(collect + Constant.RedisCache.redisSeparator + key);
    }

    /**
     * @description 删除单个缓存对象 key为Long
     * @author fancl
     * @date 2019/8/28
     * @param collect 集合的名字 key：键值 一般为id 或者为拼接的key
     * @return
     */
    public static void remove(String collect, Long key) {
        CLIENT.delete(collect + Constant.RedisCache.redisSeparator + key);
    }


    /**
     * @description 批量清除key,用于对指定的List<id> 进行清空
     * @author fancl
     * @date 2019/8/28
     * @param
     * @return
     */
    public static void removeBatch(String collect, List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            CLIENT.delete(ids.stream().map(key -> collect + Constant.RedisCache.redisSeparator + key).collect(Collectors.toList()));
        }

    }

    /**
     * 设置缓存数据
     *
     * @param key     键
     * @param hashKey 键
     * @param value   值
     */
    public static void setH(String key, String hashKey, String value) {
        HashOperations<String, String, String> hashOps = CLIENT.opsForHash();
        hashOps.put(key, hashKey, value);
    }

    /**
     * 设置缓存数据
     *
     * @param key     键
     * @param hashKey 键
     * @param value   值
     * @param endDate 过期日期
     */
    public static void setH(String key, String hashKey, String value, Date endDate) {
        HashOperations<String, String, String> hashOps = CLIENT.opsForHash();
        hashOps.put(key, hashKey, value);
        CLIENT.expireAt(key, endDate);
    }

    /**
     * 设置缓存数据
     *
     * @param key     键
     * @param hashKey 键
     * @param timeout 过期秒数
     */
    public static void setH(String key, String hashKey, String value, Long timeout) {
        HashOperations<String, String, String> hashOps = CLIENT.opsForHash();
        hashOps.put(key, hashKey, value);
        CLIENT.expire(key, timeout, TimeUnit.SECONDS);
    }

    /**
     * 获取缓存数据
     *
     * @param key     键
     * @param hashKey 键
     */
    public static <T> T getH(String key, String hashKey) {
        HashOperations<String, String, T> hashOps = CLIENT.opsForHash();
        return hashOps.get(key, hashKey);
    }

    public static void setZ(String key, String value, double score) {
        ZSetOperations<String, String> operations = CLIENT.opsForZSet();
        operations.add(key, value, score);
    }

    public static Set<String> getZ(String key, int startRange, int endRange, boolean orderByDesc) {
        ZSetOperations<String, String> operations = CLIENT.opsForZSet();
        if (orderByDesc) {
            return operations.reverseRange(key, startRange, endRange);
        } else {
            return operations.range(key, startRange, endRange);
        }
    }

    public static void removeZ(String key, String id) {
        ZSetOperations<String, String> operations = CLIENT.opsForZSet();
        operations.remove(key, id);
    }

    public static Double getScore(String key, String id) {
        ZSetOperations<String, String> operations = CLIENT.opsForZSet();
        return operations.score(key, id);
    }


    public static List<String> getL(String key) {
        ListOperations<String, String> operations = CLIENT.opsForList();
        return operations.range(key, 0, -1);
    }

    public static void setList(String key, String... val) {
        ListOperations<String, String> operations = CLIENT.opsForList();
        operations.rightPushAll(key, val);
    }

    public static void removeL(String key, long count, Object value) {
        ListOperations<String, String> operations = CLIENT.opsForList();
        operations.remove(key, count, value);
    }

    /**
     * @param key key
     * @return
     */
    @SuppressWarnings("all")
    public static boolean hasKey(@NotNull final String key) {
        return CLIENT.hasKey(key);
    }

    /**
     * 获取redis自增序号
     *
     * @param key        key
     * @param expireTime 过期时间,单位秒
     * @param delta      自增值
     */
    public static Long generate(String key, long expireTime, long delta) {
        Long increment;
        ValueOperations<String, String> opsForValue = CLIENT.opsForValue();
        if (!hasKey(key)) {
            increment = opsForValue.increment(key, delta);
            expire(key, expireTime, TimeUnit.SECONDS);
        } else {
            increment = opsForValue.increment(key, delta);
        }
        return increment;
    }

    /**
     * 分布式锁
     * https://www.cnblogs.com/linjiqin/p/8003838.html
     *
     * @param key     key
     * @param timeout 秒
     * @param process 上锁代码
     */
    public static <T> T lock(String key, long timeout, LockProcess<T> process) {
        String uuid = UUID.randomUUID().toString();
        boolean lock = lock(key, uuid, timeout);
        try {
            if (lock) {
                return process.execute();
            } else {
                throw new ServiceException(ResultCode.TIME_OUT);
            }
        } finally {
            releaseLock(key, uuid);
        }
    }

    private static boolean lock(String key, String token, long timeout) {
        // 记录上锁失败次数
        int count = 0;
        RedisCallback<String> callback = connection -> {
            JedisCommands commands = (JedisCommands) connection.getNativeConnection();
            return commands.set(key, token, "NX", "PX", timeout * 1000);
        };
        LOGGER.info("开始上锁, key：{}，token：{}", key, token);
        while (true) {
            String result = CLIENT.execute(callback);
            if ("OK".equals(result)) {
                return true;
            }
            try {
                if (count == timeout) {
                    LOGGER.info("尝试上锁超时，手动退出，count：{}，key：{}", ++count, key);
                    return false;
                }
                LOGGER.info("没拿到锁，等待下次尝试，count：{}，key：{}", ++count, key);
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                LOGGER.info("上锁失败，key：{}", key, e);
                return false;
            }
        }
    }

    private static void releaseLock(String key, String token) {
        List<String> keys = Collections.singletonList(key);
        List<String> args = Collections.singletonList(token);
        RedisCallback<Long> callback = connection -> {
            Object nativeConnection = connection.getNativeConnection();
            if (nativeConnection instanceof JedisCluster) {
                // 集群模式
                return (Long) ((JedisCluster) nativeConnection).eval(UNLOCK_LUA, keys, args);
            } else if (nativeConnection instanceof Jedis) {
                return (Long) ((Jedis) nativeConnection).eval(UNLOCK_LUA, keys, args);
            }
            return 0L;
        };
        LOGGER.info("释放锁, key：{}，token：{}", key, token);
        CLIENT.execute(callback);
    }

    /**
     * 上多个锁,并自动释放
     *
     * @param timeout 自动解锁时长,单位秒
     * @param process 上锁线程
     * @param keys    多个key
     * @param <T>     返回值类型
     * @return t
     */
    public static <T> T multiLock(long timeout, LockProcess<T> process, @NotNull String... keys) {
        String uuid = UUID.randomUUID().toString();
        boolean lock = multiLock(uuid, timeout, keys);
        try {
            if (lock) {
                return process.execute();
            } else {
                throw new ServiceException(ResultCode.TIME_OUT);
            }
        } finally {
            multiReleaseLock(uuid, keys);
        }
    }

    /**
     * 单独上锁(多个)
     *
     * @param token   校验是否同一个请求的标识
     * @param timeout 自动解锁时长,单位秒
     * @param keys    redis key
     * @return boolean
     */
    public static boolean multiLock(String token, long timeout, @NotNull String... keys) {
        // 记录上锁失败次数
        int count = 0;
        List<String> args = new ArrayList<>();
        args.add(token);
        args.add(String.valueOf(timeout));
        // 去重,并利用HashSet排序
        List<String> keyList = new ArrayList<>(new HashSet<>(Arrays.asList(keys)));
        String keysStr = Arrays.toString(keys);
        LOGGER.info("开始上锁, key：{}，token：{}", keysStr, token);
        while (true) {
            RedisCallback<Long> callback = buildRedisCallback(keyList, args, MULTI_LOCK_SCRIPT);
            Long execute = CLIENT.execute(callback);
            if (execute != null && execute > 0) {
                return true;
            }
            try {
                if (count == timeout) {
                    LOGGER.info("尝试上锁超时，手动退出，count：{}，key：{}", ++count, keysStr);
                    return false;
                }
                LOGGER.info("没拿到锁，等待下次尝试，count：{}，key：{}", ++count, keysStr);
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                LOGGER.info("上锁失败，key：{}", keysStr, e);
                return false;
            }
        }
    }

    /**
     * 单独释放锁(多个)
     *
     * @param token 校验是否同一个请求的标识,token不一致时不释放
     * @param keys  redis key
     */
    public static void multiReleaseLock(String token, @NotNull String... keys) {
        List<String> keyList = Arrays.asList(keys);
        List<String> args = Collections.singletonList(token);
        RedisCallback<Long> callback = buildRedisCallback(keyList, args, MULTI_UNLOCK_SCRIPT);
        LOGGER.info("释放锁, key：{}，token：{}", Arrays.toString(keys), token);
        CLIENT.execute(callback);
    }

    /**
     * 构建RedisCallback
     *
     * @param keyList keyList
     * @param args    args
     * @param script  script
     * @return redisCallback
     */
    public static RedisCallback<Long> buildRedisCallback(List<String> keyList, List<String> args, String script) {
        return connection -> {
            Object nativeConnection = connection.getNativeConnection();
            if (nativeConnection instanceof JedisCluster) {
                // 集群模式
                return (Long) ((JedisCluster) nativeConnection).eval(script, keyList, args);
            } else if (nativeConnection instanceof Jedis) {
                return (Long) ((Jedis) nativeConnection).eval(script, keyList, args);
            }
            return null;
        };
    }

    public interface LockProcess<T> {
        /**
         * 上锁代码块
         *
         * @return T
         */
        T execute();
    }

    /**
     * @description: json序列化时间类型数据时存在问题，使用java的序列化来存储数据
     * @param: [key, data]
     * @return: void
     * @author: xdy
     * @create: 2019-06-10 09-10
     */
    public static void set(String key, Object data) {
        CLIENT.execute(new RedisCallback<Void>() {
            @Override
            public Void doInRedis(RedisConnection connection) throws DataAccessException {
                connection.set(key.getBytes(), serialize(data));
                return null;
            }
        });
    }

    /**
     * @description: 同上，并设置缓存时间
     * @param: [key, data, timeout, unit]
     * @return: void
     * @author: xdy
     * @create: 2019-06-10 10-02
     */
    public static void set(String key, Object data, long timeout, TimeUnit unit) {
        CLIENT.execute(new RedisCallback<Void>() {
            @Override
            public Void doInRedis(RedisConnection connection) throws DataAccessException {
                connection.set(key.getBytes(), serialize(data));
                long rawTimeout = TimeoutUtils.toMillis(timeout, unit);
                connection.expire(key.getBytes(), rawTimeout);
                return null;
            }
        });
    }

    public static Object getObj(String key) {
        return CLIENT.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                byte[] data = connection.get(key.getBytes());
                return deSerialize(data);
            }
        });
    }

    /**
     * @description: 序列化对象
     * @param: [obj]
     * @return: byte[]
     * @author: xdy
     * @create: 2019-06-10 10-04
     */
    public static byte[] serialize(Object obj) {
        byte[] bytes = null;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(obj);
            bytes = baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    /**
     * @description: 反序列化对象
     * @param: [bytes]
     * @return: java.lang.Object
     * @author: xdy
     * @create: 2019-06-10 10-05
     */
    public static Object deSerialize(byte[] bytes) {
        Object obj = null;
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)
             ; ObjectInputStream ois = new ObjectInputStream(bais)) {
            obj = ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }

}
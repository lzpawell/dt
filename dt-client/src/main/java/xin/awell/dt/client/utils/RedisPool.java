package xin.awell.dt.client.utils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author lzp
 * @since 2019/4/1119:52
 */
public class RedisPool {
    public static Jedis getInstance(){
        return jedisPool.getResource();
    }


    private static JedisPool jedisPool;

    static {
        JedisPoolConfig config = new JedisPoolConfig();
        jedisPool = new JedisPool(config, "39.108.65.230", 6379, 100000, "chuyin12345");
    }
}

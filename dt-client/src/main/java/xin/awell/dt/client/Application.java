package xin.awell.dt.client;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import xin.awell.dt.client.core.ConfigDataSynchronizer;
import xin.awell.dt.client.core.JobInstanceChannel;
import xin.awell.dt.client.core.JobTriggerService;
import xin.awell.dt.client.core.ZKService.ZKService;
import xin.awell.dt.client.core.ZKService.ZKServiceImpl;
import xin.awell.dt.client.processer.BaseJobProcessor;
import xin.awell.dt.client.processer.CommonJobProcessor;
import xin.awell.dt.client.processer.ProcessorContainer;
import xin.awell.dt.client.utils.RedisPool;
import xin.awell.dt.core.domain.JobConfigDO;

import java.util.*;

/**
 * @author lzp
 * @since 2019/2/2314:27
 */
@Slf4j
public class Application {
    public static void main(String[] args) {
        Jedis jedis = RedisPool.getInstance();

        jedis.set("test", "mdzz");

        jedis.keys("*").forEach(System.out::println);

        System.out.println(jedis.get("test"));
    }
}

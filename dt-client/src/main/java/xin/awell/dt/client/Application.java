package xin.awell.dt.client;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.apache.rocketmq.remoting.exception.RemotingException;
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

import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * @author lzp
 * @since 2019/2/2314:27
 */
@Slf4j
public class Application {
    public static void main(String[] args) {
        startConsumer();
        startProducer();
    }

    public static void startProducer(){
        new Thread(()->{
            try{
                Thread.sleep(5000);

                DefaultMQProducer producer = new DefaultMQProducer("dt");
                producer.setNamesrvAddr("39.108.65.230:9876");
                producer.start();
                producer.setSendMsgTimeout(10000);

                for (int i = 0; i < 5; i++){
                    Message msg = new Message("dt",
                            "dt-demo",
                            "OrderID188",
                            "Hello world".getBytes(RemotingHelper.DEFAULT_CHARSET));
                    SendResult sendResult = producer.send(msg);
                    System.out.printf("%s%n", sendResult);
                    Thread.sleep(500);
                }
                producer.shutdown();
            }catch (Exception e){
                e.printStackTrace();
            }
        }).start();
    }

    public static void startConsumer(){
        new Thread(()->{
            try{
                DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("dt");
                consumer.setNamesrvAddr("39.108.65.230:9876");
                consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);

                //set to broadcast mode
                consumer.setMessageModel(MessageModel.BROADCASTING);

                consumer.subscribe("dt", "dt-demo");

                consumer.registerMessageListener(new MessageListenerConcurrently() {

                    @Override
                    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,
                                                                    ConsumeConcurrentlyContext context) {
                        System.out.printf(Thread.currentThread().getName() + " Receive New Messages: " + msgs + "%n");
                        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                    }
                });

                consumer.start();
                System.out.printf("Broadcast Consumer Started.%n");
            }catch (Exception e){
                e.printStackTrace();
            }
        }).start();
    }
}

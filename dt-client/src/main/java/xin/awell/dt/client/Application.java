package xin.awell.dt.client;

import lombok.extern.slf4j.Slf4j;
import xin.awell.dt.client.core.ConfigDataSynchronizer;
import xin.awell.dt.client.core.JobInstanceChannel;
import xin.awell.dt.client.core.JobTriggerService;
import xin.awell.dt.client.core.ZKService.ZKService;
import xin.awell.dt.client.core.ZKService.ZKServiceImpl;
import xin.awell.dt.client.processer.BaseJobProcessor;
import xin.awell.dt.client.processer.CommonJobProcessor;
import xin.awell.dt.client.processer.ProcessorContainer;
import xin.awell.dt.core.domain.JobConfigDO;

import java.util.*;

/**
 * @author lzp
 * @since 2019/2/2314:27
 */
@Slf4j
public class Application {
    public static void main(String[] args) throws Exception {
        ZKService zkService = new ZKServiceImpl("39.108.65.230:2181", "9a46b292-4e7d-4f62-a372-2c4e02ff5cce");
        zkService.addListener(instanceStatus -> System.out.println("状态转变为： " + instanceStatus.toString()));

        zkService.start();

        ConfigDataSynchronizer configDataSynchronizer = new ConfigDataSynchronizer(zkService.getCuratorInstance(), "9a46b292-4e7d-4f62-a372-2c4e02ff5cce");

        configDataSynchronizer.subscribeDataUpdate(newJobConfigDOList -> {
            System.out.println("config data updated!");
            Optional.ofNullable(newJobConfigDOList).orElse(Collections.emptyList())
                    .forEach(jobConfigDO -> System.out.println(jobConfigDO.toString()));
        });


        configDataSynchronizer.init();

        JobTriggerService triggerService = new JobTriggerService(zkService, configDataSynchronizer);
        triggerService.start();


        BaseJobProcessor processor = ProcessorContainer.getCommonJobProcessor("xin.awell.dt.server.processor.TCommonJobProcessor");
        processor = ProcessorContainer.getCommonJobProcessor("xin.awell.dt.server.processor.TCommonJobProcessor");
        processor = ProcessorContainer.getSimpleJobProcessor("xin.awell.dt.server.processor.TSimpleJobProcessor");
        processor = ProcessorContainer.getSimpleJobProcessor("xin.awell.dt.server.processor.MySimpleJobProcessor");
        processor = ProcessorContainer.getSimpleJobProcessor("xin.awell.dt.client.BalalTSimpleJobProcessor");
        processor = ProcessorContainer.getParallelJobProcessor("xin.awell.dt.server.processor.TParallelJobProcessor");


        Scanner scanner = new Scanner(System.in);
        boolean quit = false;
        while (!quit){
            String input = scanner.nextLine();
            if(Objects.equals(input, "quit")){
                quit = true;
            }
        }

        triggerService.shutdown();
        zkService.shutdown();
    }
}

package xin.awell.dt.client.processer;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;

/**
 * @author lzp
 * @since 2019/4/12:00
 */

@Slf4j
public class ProcessorContainer {
    private static HashMap<String, CommonJobProcessor> commonJobProcessorHashMap = new HashMap<>();
    private static HashMap<String, SimpleJobProcessor> simpleJobProcessorHashMap = new HashMap<>();
    private static HashMap<String, ParallelJobProcessor> parallelJobProcessorHashMap = new HashMap<>();


    private static BaseJobProcessor getProcessorInternal(String fullPathName) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        return (BaseJobProcessor) Class.forName(fullPathName).newInstance();
    }

    public static CommonJobProcessor getCommonJobProcessor(String fullPathName){
        CommonJobProcessor processor = commonJobProcessorHashMap.get(fullPathName);
        if(processor == null){
            synchronized (commonJobProcessorHashMap){
                if(commonJobProcessorHashMap.get(fullPathName) == null){
                    try {
                        processor = (CommonJobProcessor) getProcessorInternal(fullPathName);
                        commonJobProcessorHashMap.put(fullPathName, processor);
                    } catch (Exception e) {
                        log.error("get common job processor error! msg {}", e.getMessage());
                        processor = null;
                    }
                }
            }
        }

        return processor;
    }

    public static SimpleJobProcessor getSimpleJobProcessor(String fullPathName){
        SimpleJobProcessor processor = simpleJobProcessorHashMap.get(fullPathName);
        if(processor == null){
            synchronized (simpleJobProcessorHashMap){
                if(simpleJobProcessorHashMap.get(fullPathName) == null){
                    try {
                        processor = (SimpleJobProcessor) getProcessorInternal(fullPathName);
                        simpleJobProcessorHashMap.put(fullPathName, processor);
                    } catch (Exception e) {
                        log.error("get simple job processor error! {}", e.getMessage());
                        processor = null;
                    }
                }
            }
        }

        return processor;
    }

    public static ParallelJobProcessor getParallelJobProcessor(String fullPathName){
        ParallelJobProcessor processor = parallelJobProcessorHashMap.get(fullPathName);
        if(processor == null){
            synchronized (parallelJobProcessorHashMap){
                if(parallelJobProcessorHashMap.get(fullPathName) == null){
                    try {
                        processor = (ParallelJobProcessor) getProcessorInternal(fullPathName);
                        parallelJobProcessorHashMap.put(fullPathName, processor);
                    } catch (Exception e) {
                        log.error("get parallel job processor error! {}", e.getMessage());
                        processor = null;
                    }
                }
            }
        }

        return processor;
    }

}

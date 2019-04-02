package xin.awell.dt.client.processer;

import java.util.HashMap;

/**
 * @author lzp
 * @since 2019/4/12:00
 */
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
                        e.printStackTrace();
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
                        e.printStackTrace();
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
                        e.printStackTrace();
                    }
                }
            }
        }

        return processor;
    }

    public static BaseJobProcessor getJobProcessor(String fullPathName){
        return null;
    }
}

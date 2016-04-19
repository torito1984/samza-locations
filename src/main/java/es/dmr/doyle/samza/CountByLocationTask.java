package es.dmr.doyle.samza;

import org.apache.samza.config.Config;
import org.apache.samza.storage.kv.Entry;
import org.apache.samza.storage.kv.KeyValueIterator;
import org.apache.samza.storage.kv.KeyValueStore;
import org.apache.samza.system.IncomingMessageEnvelope;
import org.apache.samza.system.OutgoingMessageEnvelope;
import org.apache.samza.system.SystemStream;
import org.apache.samza.task.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dmartinez on 4/16/16.
 */
public class CountByLocationTask implements StreamTask, InitableTask, WindowableTask {
    private KeyValueStore<String, Integer> store;

    private static final String STORE_NAME = "count-by-location";
    private static final String OUTPUT_SYSTEM_NAME = "kafka";
    private static final String OUTPUT_STREAM_NAME = "location-occurrences";

    public void init(Config config, TaskContext context) {
        this.store = (KeyValueStore<String, Integer>) context.getStore(STORE_NAME);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void process(IncomingMessageEnvelope envelope, MessageCollector collector, TaskCoordinator coordinator) {
        Map<String, Object> message = (Map<String, Object>)envelope.getMessage();
        String place = (String)message.get("place");


        Object countByPlaceObject = store.get(place);
        Integer countByPlace = 0;

        if(countByPlaceObject != null)
            countByPlace = (Integer)countByPlaceObject;

        countByPlace++;

        store.put(place, countByPlace);
    }

    /**
     * Every minute we collect all the counts and publish them
     *
     * @param messageCollector
     * @param taskCoordinator
     * @throws Exception
     */
    @Override
    public void window(MessageCollector messageCollector, TaskCoordinator taskCoordinator) throws Exception {

        KeyValueIterator<String, Integer> values = store.all();

        while(values.hasNext()){
            Entry<String, Integer> value = values.next();
            if(value.getValue() > 0) {
                Map<String, Object> output = new HashMap<String, Object>();
                Map<String, Object> outputCountByPlace = new HashMap<String, Object>();
                outputCountByPlace.put("place", value.getKey());
                outputCountByPlace.put("count", value.getValue());
                output.put("data", outputCountByPlace);

                messageCollector.send(new OutgoingMessageEnvelope(new SystemStream(OUTPUT_SYSTEM_NAME, OUTPUT_STREAM_NAME), value.getKey(), output));
                // reset
                store.put(value.getKey(), 0);
            }
        }
    }
}
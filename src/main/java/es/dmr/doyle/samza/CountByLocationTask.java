package es.dmr.doyle.samza;

import org.apache.samza.config.Config;
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
public class CountByLocationTask implements StreamTask, InitableTask {
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

        Map<String, Object> output = new HashMap<String, Object>();

        Map<String, String> outputCountByUserId = new HashMap<String, String>();
        outputCountByUserId.put("place", place);
        outputCountByUserId.put("count", countByPlace.toString());
        output.put("data", outputCountByUserId);

        collector.send(new OutgoingMessageEnvelope(new SystemStream(OUTPUT_SYSTEM_NAME, OUTPUT_STREAM_NAME), place, output));
    }
}
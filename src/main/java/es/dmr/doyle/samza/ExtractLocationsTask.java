package es.dmr.doyle.samza;

import es.dmr.doyle.samza.nlp.NLPConceptExtractor;
import org.apache.samza.config.Config;
import org.apache.samza.system.IncomingMessageEnvelope;
import org.apache.samza.system.OutgoingMessageEnvelope;
import org.apache.samza.system.SystemStream;
import org.apache.samza.task.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dmartinez on 4/16/16.
 */
public class ExtractLocationsTask implements StreamTask, InitableTask {

    private static final String OUTPUT_SYSTEM_NAME = "kafka";
    private static final String OUTPUT_STREAM_NAME = "group-by-location";
    private NLPConceptExtractor extractor;

    public void init(Config config, TaskContext context) {
        try {
            extractor = new NLPConceptExtractor();
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void process(IncomingMessageEnvelope envelope, MessageCollector collector, TaskCoordinator coordinator) {
        String text = (String)envelope.getMessage();

        extractor.getLocations(text).stream().forEach(place -> {
            Map<String, Object> output = new HashMap<String, Object>();
            output.put("place", place);
            output.put("count", "1");
            collector.send(new OutgoingMessageEnvelope(new SystemStream(OUTPUT_SYSTEM_NAME, OUTPUT_STREAM_NAME), place, output));
        });
    }
}
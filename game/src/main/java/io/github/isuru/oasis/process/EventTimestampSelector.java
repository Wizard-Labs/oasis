package io.github.isuru.oasis.process;

import io.github.isuru.oasis.Event;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;

/**
 * @author iweerarathna
 */
public class EventTimestampSelector<E extends Event> extends AscendingTimestampExtractor<E> {
    @Override
    public long extractAscendingTimestamp(E element) {
        return element.getTimestamp();
    }
}

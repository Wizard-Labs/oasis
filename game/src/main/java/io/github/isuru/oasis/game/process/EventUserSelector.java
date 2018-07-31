package io.github.isuru.oasis.game.process;

import io.github.isuru.oasis.model.Event;
import org.apache.flink.api.java.functions.KeySelector;

/**
 * @author iweerarathna
 */
public class EventUserSelector<E extends Event> implements KeySelector<E, Long> {

    @Override
    public Long getKey(E value) {
        return value.getUser();
    }
}
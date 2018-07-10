package io.github.isuru.oasis.model;

import io.github.isuru.oasis.Event;

import java.util.Map;

/**
 * @author iweerarathna
 */
public class MilestoneEvent implements Event {

    private final Milestone milestone;
    private final int level;
    private final Event causedEvent;
    private final Long user;

    public MilestoneEvent(Long userId, Milestone milestone, int level, Event causedEvent) {
        this.milestone = milestone;
        this.level = level;
        this.causedEvent = causedEvent;
        this.user = userId;
    }

    public int getLevel() {
        return level;
    }

    public Milestone getMilestone() {
        return milestone;
    }

    @Override
    public Map<String, Object> getAllFieldValues() {
        return causedEvent.getAllFieldValues();
    }

    @Override
    public void setFieldValue(String fieldName, Object value) {
        throw new RuntimeException("Milestone events cannot be modified!");
    }

    @Override
    public Object getFieldValue(String fieldName) {
        if (fieldName.equals("level")) {
            return level;
        } else if (fieldName.equals("milestone")) {
            return milestone;
        } else {
            return null;
        }
    }

    @Override
    public String getEventType() {
        return causedEvent.getEventType();
    }

    @Override
    public long getTimestamp() {
        return causedEvent.getTimestamp();
    }

    @Override
    public long getUser() {
        return user;
    }

    @Override
    public Long getExternalId() {
        return causedEvent.getExternalId();
    }

    @Override
    public Long getUserId(String fieldName) {
        return causedEvent.getUserId(fieldName);
    }

    @Override
    public Long getScope(int level) {
        return causedEvent.getScope(level);
    }

    @Override
    public String toString() {
        return getEventType() + "#" + getExternalId();
    }
}

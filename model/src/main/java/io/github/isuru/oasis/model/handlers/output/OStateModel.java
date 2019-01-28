package io.github.isuru.oasis.model.handlers.output;

import io.github.isuru.oasis.model.events.JsonEvent;

public class OStateModel {

    private Long userId;
    private Long teamId;
    private Long teamScopeId;
    private Long stateId;

    private Integer previousState;
    private Integer currentState;
    private String currentValue;
    private Double currentPoints;
    private Long ts;
    private long prevStateChangedAt;
    private JsonEvent event;
    private String extId;
    private Integer sourceId;
    private boolean currency;
    private Integer gameId;

    public Integer getGameId() {
        return gameId;
    }

    public void setGameId(Integer gameId) {
        this.gameId = gameId;
    }

    public long getPrevStateChangedAt() {
        return prevStateChangedAt;
    }

    public void setPrevStateChangedAt(long prevStateChangedAt) {
        this.prevStateChangedAt = prevStateChangedAt;
    }

    public Integer getPreviousState() {
        return previousState;
    }

    public void setPreviousState(Integer previousState) {
        this.previousState = previousState;
    }

    public boolean isCurrency() {
        return currency;
    }

    public void setCurrency(boolean currency) {
        this.currency = currency;
    }

    public Integer getSourceId() {
        return sourceId;
    }

    public void setSourceId(Integer sourceId) {
        this.sourceId = sourceId;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public Long getTeamScopeId() {
        return teamScopeId;
    }

    public void setTeamScopeId(Long teamScopeId) {
        this.teamScopeId = teamScopeId;
    }

    public Double getCurrentPoints() {
        return currentPoints;
    }

    public void setCurrentPoints(Double currentPoints) {
        this.currentPoints = currentPoints;
    }

    public String getExtId() {
        return extId;
    }

    public void setExtId(String extId) {
        this.extId = extId;
    }

    public JsonEvent getEvent() {
        return event;
    }

    public void setEvent(JsonEvent event) {
        this.event = event;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getStateId() {
        return stateId;
    }

    public void setStateId(Long stateId) {
        this.stateId = stateId;
    }

    public Integer getCurrentState() {
        return currentState;
    }

    public void setCurrentState(Integer currentState) {
        this.currentState = currentState;
    }

    public String getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(String currentValue) {
        this.currentValue = currentValue;
    }

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }
}
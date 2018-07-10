package io.github.isuru.oasis.model;

import java.io.Serializable;

/**
 * @author iweerarathna
 */
public class FieldCalculator implements Serializable {

    private String forEvent;
    private String fieldName;
    private Serializable expression;

    public String getForEvent() {
        return forEvent;
    }

    public void setForEvent(String forEvent) {
        this.forEvent = forEvent;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public Serializable getExpression() {
        return expression;
    }

    public void setExpression(Serializable expression) {
        this.expression = expression;
    }
}

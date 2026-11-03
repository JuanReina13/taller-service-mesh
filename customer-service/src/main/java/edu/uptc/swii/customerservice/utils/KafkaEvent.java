package edu.uptc.swii.customerservice.utils;

public class KafkaEvent {
    private String operation;
    private String payload; // JSON de la entidad o dato simple

    public KafkaEvent() {
    }

    public KafkaEvent(String operation, String payload) {
        this.operation = operation;
        this.payload = payload;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}
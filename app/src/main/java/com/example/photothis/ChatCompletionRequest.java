package com.example.photothis;

import java.util.List;
import java.util.Map;

public class ChatCompletionRequest {
    private String model;
    private List<Map<String, String>> messages;
    private double temperature;

    public ChatCompletionRequest(String model, List<Map<String, String>> messages, double temperature) {
        this.model = model;
        this.messages = messages;
        this.temperature = temperature;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<Map<String, String>> getMessages() {
        return messages;
    }

    public void setMessages(List<Map<String, String>> messages) {
        this.messages = messages;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }
}

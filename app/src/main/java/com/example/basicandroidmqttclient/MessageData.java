package com.example.basicandroidmqttclient;

public class MessageData {
    String luminosity;
    String time;

    public MessageData(String luminosity, String time) {
        this.luminosity = luminosity;
        this.time = time;
    }

    @Override
    public String toString() {
        return "Luminosidade: " + luminosity + "\nHorário: " + time;
    }
}
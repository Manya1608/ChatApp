package com.example.chatapp;

class BaseMessage {
    String message, time, status;

    BaseMessage(String message, String time, String status) {
        this.message = message;
        this.time = time;
        this.status = status;
    }

    String getMessage() {
        return message;
    }

    String getTime() {
        return time;
    }

    String getStatus() {
        return status;
    }
}

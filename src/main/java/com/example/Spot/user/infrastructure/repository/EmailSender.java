package com.example.Spot.user.infrastructure.repository;

public interface EmailSender {
    void send(String to, String subject, String body);
}

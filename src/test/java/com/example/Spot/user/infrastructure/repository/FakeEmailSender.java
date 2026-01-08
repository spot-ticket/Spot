package com.example.Spot.user.infrastructure.repository;



import com.example.Spot.user.infrastructure.respository.EmailSender;

import java.util.concurrent.atomic.AtomicReference;

public class FakeEmailSender implements EmailSender {

    private final AtomicReference<EmailPayload> last = new AtomicReference<>();

    @Override
    public void send(String to, String subject, String body) {
        last.set(new EmailPayload(to, subject, body));
    }

    public EmailPayload lastEmail() {
        EmailPayload p = last.get();
        if (p == null) throw new IllegalStateException("아직 발송된 메일이 없습니다.");
        return p;
    }

    public record EmailPayload(String to, String subject, String body) {}
}
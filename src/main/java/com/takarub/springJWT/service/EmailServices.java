package com.takarub.springJWT.service;

import com.takarub.springJWT.dto.MailBody;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServices {

    private final JavaMailSender javaMailSender;

    public void sendSimpleMessage(MailBody mailBody){
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(mailBody.to());
        message.setText(mailBody.text());
        message.setFrom("mahmoodselawe5@gmail.com");
        message.setSubject(mailBody.subject());

        javaMailSender.send(message);
    }
}

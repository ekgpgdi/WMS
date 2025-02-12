package com.dahye.wms.customer.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
  private final JavaMailSender emailSender;
  private static final String FROM = "dahae80912@gmail.com";
  private static final String SUBJECT = "[인증 코드] 이메일 인증 코드 ";
  private static final String TEXT = "안녕하세요. 하단 인증 코드를 입력해주세요 \n" + "CODE : ";

  public void sendMail(@NonNull String email, @NonNull String certCode) {
    SimpleMailMessage simpleMessage = new SimpleMailMessage();
    simpleMessage.setFrom(FROM);
    simpleMessage.setTo(email);
    simpleMessage.setSubject(SUBJECT);
    simpleMessage.setText(TEXT + certCode);
    emailSender.send(simpleMessage);
  }
}

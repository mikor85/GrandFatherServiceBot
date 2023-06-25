package mi.corvik.service;

import mi.corvik.dto.MailParams;

public interface MailSenderService {
    void send(MailParams mailParams);
}
package mi.corvik.service;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

// For sending answers from node to broker
public interface ProducerService {

    void producerAnswer(SendMessage sendMessage);
}
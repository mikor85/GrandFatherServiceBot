package mi.corvik.service;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface MainService {

    void processTextMessage(Update update);

}
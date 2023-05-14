package mi.corvik.service;

import mi.corvik.entity.AppDocument;
import mi.corvik.entity.AppPhoto;
import mi.corvik.service.enums.LinkType;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface FileService {
    AppDocument processDoc(Message telegramMessage);

    AppPhoto processPhoto(Message telegramMessage);

    String generateLink(Long fileId, LinkType linkType);
}
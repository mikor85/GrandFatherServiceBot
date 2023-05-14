package mi.corvik.service.impl;

import lombok.extern.log4j.Log4j;
import mi.corvik.dao.AppUserDAO;
import mi.corvik.dao.RawDataDAO;
import mi.corvik.entity.AppDocument;
import mi.corvik.entity.AppPhoto;
import mi.corvik.entity.AppUser;
import mi.corvik.entity.RawData;
import mi.corvik.exceptions.UploadFileException;
import mi.corvik.service.FileService;
import mi.corvik.service.MainService;
import mi.corvik.service.ProducerService;
import mi.corvik.service.enums.LinkType;
import mi.corvik.service.enums.ServiceCommand;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import static mi.corvik.entity.enums.UserState.BASIC_STATE;
import static mi.corvik.entity.enums.UserState.WAIT_FOR_EMAIL_STATE;
import static mi.corvik.service.enums.ServiceCommand.*;

// This is a link between the database and the service that sends messages from the queue RabbitMQ
@Service
@Log4j
public class MainServiceImpl implements MainService {

    private final RawDataDAO rawDataDAO;
    private final ProducerService producerService;
    private final AppUserDAO appUserDAO;

    private final FileService fileService;

    public MainServiceImpl(RawDataDAO rawDataDAO,
                           ProducerService producerService,
                           AppUserDAO appUserDAO,
                           FileService fileService) {
        this.rawDataDAO = rawDataDAO;
        this.producerService = producerService;
        this.appUserDAO = appUserDAO;
        this.fileService = fileService;
    }

    @Override
    public void processTextMessage(Update update) {
        saveRawData(update);
        var appUser = findOrSaveAppUser(update);
        var userState = appUser.getState();
        var text = update.getMessage().getText();
        var output = "";

        var serviceCommand = ServiceCommand.fromValue(text);
        if (CANCEL.equals(serviceCommand)) {
            output = cancelProcess(appUser);
        } else if (BASIC_STATE.equals(userState)) {
            output = processServiceCommand(appUser, text);
        } else if (WAIT_FOR_EMAIL_STATE.equals(userState)) {
            // TODO добавить обработку емайла
        } else {
            log.debug("Unknown user state: " + userState);
            output = "Unknown error! Please enter command /cancel and try again!";
        }

        var chatId = update.getMessage().getChatId();
        sendAnswer(output, chatId);
    }

    @Override
    public void processDocMessage(Update update) {
        saveRawData(update);
        var appUser = findOrSaveAppUser(update);
        var chatId = update.getMessage().getChatId();
        if (isNotAllowToSendContent(chatId, appUser)) {
            return;
        }

        try {
            AppDocument doc = fileService.processDoc(update.getMessage());
            String link = fileService.generateLink(doc.getId(), LinkType.GET_DOC);
            var answer = "Document has been successfully loaded! "
                    + "Download link: " + link;
            sendAnswer(answer, chatId);
        } catch (UploadFileException uploadFileException) {
            log.error(uploadFileException);
            String error = "Unfortunately, the file upload failed. Please try again later.";
            sendAnswer(error, chatId);
        }
    }

    @Override
    public void processPhotoMessage(Update update) {
        saveRawData(update);
        var appUser = findOrSaveAppUser(update);
        var chatId = update.getMessage().getChatId();
        if (isNotAllowToSendContent(chatId, appUser)) {
            return;
        }

        try {
            AppPhoto photo = fileService.processPhoto(update.getMessage());
            String link = fileService.generateLink(photo.getId(), LinkType.GET_PHOTO);
            var answer = "Photo has been successfully loaded! "
                    + "Download link: " + link;
            sendAnswer(answer, chatId);
        } catch (UploadFileException uploadFileException) {
            log.error(uploadFileException);
            String error = "Unfortunately, the photo upload failed. Please try again later.";
            sendAnswer(error, chatId);
        }
    }

    private boolean isNotAllowToSendContent(Long chatId, AppUser appUser) {
        var userState = appUser.getState();
        if (!appUser.getIsActive()) {
            var error = "For loading content please register " +
                    "or activate your account profile.";
            sendAnswer(error, chatId);
            return true;
        } else if (!BASIC_STATE.equals(userState)) {
            var error = "For loading files please cancel a current command with /cancel.";
            sendAnswer(error, chatId);
            return true;
        }
        return false;
    }

    private void sendAnswer(String output, Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(output);
        producerService.producerAnswer(sendMessage);
    }

    private String processServiceCommand(AppUser appUser, String cmd) {
        var serviceCommand = ServiceCommand.fromValue(cmd);
        if (REGISTRATION.equals(serviceCommand)) {
            // TODO добавить регистрацию
            return "Temporarily unavailable.";
        } else if (HELP.equals(serviceCommand)) {
            return help();
        } else if (START.equals(serviceCommand)) {
            return "Hello! For available commands list please enter /help";
        } else {
            return "Unknown command! For available commands list please enter /help";
        }
    }

    private String help() {
        return "List of available commands:\n"
                + "/cancel - to cancel execution of current command\n"
                + "/registration - регистрация пользователя.";
    }

    private String cancelProcess(AppUser appUser) {
        appUser.setState(BASIC_STATE);
        appUserDAO.save(appUser);
        return "Command has been canceled";
    }

    private AppUser findOrSaveAppUser(Update update) {
        User telegramUser = update.getMessage().getFrom();
        AppUser persistenceAppUser = appUserDAO.findAppUserByTelegramUserId(telegramUser.getId());
        if (persistenceAppUser == null) {
            AppUser transientAppUser = AppUser.builder()
                    .telegramUserId(telegramUser.getId())
                    .userName(telegramUser.getUserName())
                    .firstName(telegramUser.getFirstName())
                    .lastName(telegramUser.getLastName())
                    // TODO изменить значение по умолчанию после добавления регистрации
                    .isActive(true)
                    .state(BASIC_STATE)
                    .build();
            return appUserDAO.save(transientAppUser);
        }
        return persistenceAppUser;
    }

    private void saveRawData(Update update) {
        RawData rawData = RawData.builder()
                .event(update)
                .build();
        rawDataDAO.save(rawData);
    }
}
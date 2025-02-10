package backend.academy.bot.commands;

import backend.academy.bot.ScrapperHttpClient;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class StartCommand implements CommandHandler {

    private static final Logger log = LoggerFactory.getLogger(StartCommand.class);
    private final ScrapperHttpClient scrapperHttpClient;

    public StartCommand(ScrapperHttpClient scrapperHttpClient) {
        this.scrapperHttpClient = scrapperHttpClient;
    }



    @Override
    public String command() {
        return "/start";
    }

    @Override
    public String description() {
        return "Зарегистрировать чат для отслеживания ссылок";
    }

    @Override
    public SendMessage handle(Update update) {
        long chatId = update.message().chat().id();
        scrapperHttpClient.registerChat(chatId)
            .doOnError(err -> log.error("Ошибка регистрации чата: " + err.getMessage()))
            .subscribe();

        return new SendMessage(chatId, "Добро пожаловать! Используйте /help для списка команд.");
    }
}

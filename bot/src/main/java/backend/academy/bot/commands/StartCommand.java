package backend.academy.bot.commands;

import backend.academy.bot.clients.ScrapperHttpClient;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class StartCommand implements CommandHandler {

    private final ScrapperHttpClient scrapperHttpClient;

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
        scrapperHttpClient
                .registerChat(chatId)
                .doOnError(err -> log.error("Ошибка регистрации чата: {}", err.getMessage()))
                .subscribe();

        return new SendMessage(chatId, "Добро пожаловать! Используйте /help для списка команд.");
    }
}

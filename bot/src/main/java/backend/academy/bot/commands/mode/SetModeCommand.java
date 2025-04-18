package backend.academy.bot.commands.mode;

import backend.academy.bot.clients.ScrapperHttpClient;
import backend.academy.bot.commands.CommandHandler;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SetModeCommand implements CommandHandler {

    private final ScrapperHttpClient scrapperHttpClient;

    @Override
    public String command() {
        return "/setmode";
    }

    @Override
    public String description() {
        return "Устанавливает режим уведомлений: 'immediate' или 'digest'";
    }

    @Override
    public SendMessage handle(Update update) {
        Long chatId = update.message().chat().id();
        String[] parts = update.message().text().split("\\s+");

        if (parts.length < 2) {
            return new SendMessage(chatId, "Пожалуйста, укажите режим: /setmode immediate или /setmode digest");
        }

        String modeStr = parts[1].toLowerCase();

        scrapperHttpClient.setChatMode(chatId, modeStr).block();
        log.info("Режим изменён");
        return new SendMessage(chatId, "Режим уведомлений установлен на: " + modeStr);
    }
}

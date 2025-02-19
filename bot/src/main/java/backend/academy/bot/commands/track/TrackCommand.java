package backend.academy.bot.commands.track;

import backend.academy.bot.commands.CommandHandler;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TrackCommand implements CommandHandler {

    private final TrackDialog trackDialog;

    public TrackCommand(TrackDialog trackDialog) {
        this.trackDialog = trackDialog;
    }

    @Override
    public String command() {
        return "/track";
    }

    @Override
    public String description() {
        return "Добавить новую отслеживаемую ссылку";
    }

    @Override
    public SendMessage handle(Update update) {
        Long chatId = update.message().chat().id();
        String messageText = update.message().text().trim();

        log.info("Обработка команды /track: chatId={}, сообщение={}", chatId, messageText);

        if (messageText.startsWith("/track")) {
            return trackDialog.handle(update);
        }

        log.warn("Команда /track введена без ссылки: chatId={}", chatId);
        return new SendMessage(chatId, "Команда /track ожидает URL.")
                .replyToMessageId(update.message().messageId());
    }
}

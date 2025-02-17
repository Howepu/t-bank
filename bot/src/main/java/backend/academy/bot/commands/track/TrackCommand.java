package backend.academy.bot.commands.track;

import backend.academy.bot.commands.CommandHandler;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.stereotype.Component;

@Component
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
        String messageText = update.message().text().trim();

        // Проверяем, есть ли URL сразу после команды /track
        if (messageText.startsWith("/track")) {
            String url = messageText.replace("/track", "").trim();

            // Если URL не пустой, сразу передаем в TrackDialog
            if (!url.isEmpty()) {
                return trackDialog.handleUrl(update, url);
            } else {
                // Если URL нет, начинаем диалог с ввода URL
                return trackDialog.handle(update);
            }
        }

        // Обработка случая, когда команда не /track (не должно быть такого, но для безопасности)
        return new SendMessage(update.message().chat().id(), "Команда /track ожидает URL.")
            .replyToMessageId(update.message().messageId());
    }
}

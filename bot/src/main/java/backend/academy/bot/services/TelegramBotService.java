package backend.academy.bot.services;

import backend.academy.bot.BotConfig;
import backend.academy.bot.commands.CommandHandler;
import backend.academy.bot.commands.TrackDialog;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SetMyCommands;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TelegramBotService {

    private final TelegramBot telegramBot;
    private final Map<String, CommandHandler> commandHandlers;
    private final TrackDialog trackDialog;

    public TelegramBotService(List<CommandHandler> commandHandlers, TrackDialog trackDialog) {
        String botToken = System.getenv("TELEGRAM_TOKEN");
        this.telegramBot = new TelegramBot(botToken);
        this.commandHandlers = commandHandlers.stream()
            .collect(Collectors.toMap(CommandHandler::command, handler -> handler));
        this.trackDialog = trackDialog;
    }

    @PostConstruct
    public void init() {
        telegramBot.execute(createMenuCommand());
        telegramBot.setUpdatesListener(updates -> {
            try {
                if (updates.isEmpty()) {
                    log.info("Нет новых обновлений.");
                    return UpdatesListener.CONFIRMED_UPDATES_ALL;
                }

                updates.forEach(this::processUpdate);
                return UpdatesListener.CONFIRMED_UPDATES_ALL;
            } catch (Exception e) {
                log.error("Ошибка при обработке обновлений: " + e.getMessage());
                e.printStackTrace();
                return UpdatesListener.CONFIRMED_UPDATES_ALL;
            }
        });
    }

    void processUpdate(Update update) {
        if (update.message() == null || update.message().text() == null) {
            log.info("Получено обновление без текста сообщения.");
            return;
        }

        long chatId = update.message().chat().id();
        String messageText = update.message().text().trim();
        log.info("Получено сообщение от chatId: {} с текстом: {}", chatId, messageText);

        // Обработка команд
        Optional<CommandHandler> handlerOpt = commandHandlers.values().stream()
            .filter(handler -> messageText.startsWith(handler.command()))
            .findFirst();

        if (handlerOpt.isPresent()) {
            CommandHandler handler = handlerOpt.get();
            SendMessage response = handler.handle(update);
            telegramBot.execute(response);
        } else if (messageText.startsWith("/")) {
            telegramBot.execute(new SendMessage(chatId, "Неизвестная команда: " + messageText + ". Введите /help"));
            log.error("Неизвестная команда от пользователя {}: {}", chatId, messageText);
            throw new IllegalArgumentException("Неизвестная команда: " + messageText);
        } else {
            // Если это не команда, передаем в TrackDialog
            SendMessage response = trackDialog.handle(update);
            telegramBot.execute(response);
        }
    }


    private SetMyCommands createMenuCommand() {
        return new SetMyCommands(commandHandlers.values().stream().map(command -> new BotCommand(
            command.command(),
            command.description()
        )).toArray(BotCommand[]::new));
    }
}





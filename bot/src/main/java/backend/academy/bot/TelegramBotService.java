package backend.academy.bot;

import backend.academy.bot.commands.CommandHandler;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TelegramBotService {

    private final TelegramBot telegramBot;
    private final Map<String, CommandHandler> commandHandlers;

    public TelegramBotService(BotConfig botConfig, List<CommandHandler> commandHandlers) {
        String botToken = System.getenv("TELEGRAM_TOKEN");
        this.telegramBot = new TelegramBot(botToken);
        this.commandHandlers = commandHandlers.stream()
            .collect(Collectors.toMap(CommandHandler::command, handler -> handler));
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(updates -> {
            try {
                if (updates.isEmpty()) {
                    System.out.println("Нет новых обновлений.");
                    return UpdatesListener.CONFIRMED_UPDATES_ALL;
                }

                updates.forEach(this::processUpdate);
                return UpdatesListener.CONFIRMED_UPDATES_ALL;
            } catch (Exception e) {
                System.err.println("Ошибка при обработке обновлений: " + e.getMessage());
                e.printStackTrace();
                return UpdatesListener.CONFIRMED_UPDATES_ALL;
            }
        });
    }

    private void processUpdate(Update update) {
        // Проверяем, есть ли сообщение и текст
        if (update.message() == null || update.message().text() == null) {
            System.out.println("Получено обновление без текста сообщения.");
            return;
        }

        // Извлекаем информацию о сообщении
        long chatId = update.message().chat().id();
        String messageText = update.message().text();
        System.out.println("Получено сообщение от chatId: " + chatId + " с текстом: " + messageText);

        // Найдем обработчик для команды
        Optional<CommandHandler> handlerOpt = Optional.ofNullable(commandHandlers.get(messageText));

        // Если обработчик найден, обрабатываем команду, иначе отправляем сообщение с ошибкой
        SendMessage response = handlerOpt
            .map(handler -> handler.handle(update))
            .orElseGet(() -> new SendMessage(chatId, "Неизвестная команда. Используйте /help."));

        // Отправляем ответ пользователю
        telegramBot.execute(response);
    }
}

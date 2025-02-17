package backend.academy.bot.commands.track;

import backend.academy.bot.dto.AddLinkRequest;
import backend.academy.bot.clients.ScrapperHttpClient;
import backend.academy.bot.dto.ListLinksResponse;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class TrackDialog {

    private final ScrapperHttpClient scrapperHttpClient;
    private final Map<Long, State> userStates = new ConcurrentHashMap<>();
    private final Map<Long, TrackedData> userData = new ConcurrentHashMap<>();

    public TrackDialog(ScrapperHttpClient scrapperHttpClient) {
        this.scrapperHttpClient = scrapperHttpClient;
    }

    private enum State {
        AWAITING_URL,
        AWAITING_TAGS,
        AWAITING_FILTERS,
        COMPLETE
    }

    private static class TrackedData {
        String url;
        List<String> tags;
        List<String> filters;

        @Override
        public String toString() {
            return "TrackedData{" +
                "filters=" + filters +
                ", url='" + url + '\'' +
                ", tags=" + tags +
                '}';
        }
    }

    // Основной метод для обработки всех сообщений
    public SendMessage handle(Update update) {
        Long chatId = update.message().chat().id();
        String messageText = update.message().text().trim();
        int replyTo = update.message().messageId();

        // Если сообщение не начинается с /track и нет активного диалога, игнорируем
        if (!messageText.startsWith("/track") && !userStates.containsKey(chatId)) {
            return new SendMessage(chatId, "").replyToMessageId(replyTo);
        }

        // Инициализируем состояния и данные пользователя, если их еще нет
        userStates.putIfAbsent(chatId, State.AWAITING_URL);
        TrackedData data = userData.computeIfAbsent(chatId, k -> new TrackedData());

        switch (userStates.get(chatId)) {

            case AWAITING_URL:
                if (messageText.startsWith("/track")) {
                    messageText = messageText.replace("/track", "").trim();
                }

                if (!messageText.isEmpty()) {
                    if (!isValidUrl(messageText)) {
                        return new SendMessage(chatId, "Неверный формат ссылки. Поддерживаются только GitHub и StackOverflow.")
                            .replyToMessageId(replyTo);
                        }


                    data.url = messageText;
                    userStates.put(chatId, State.AWAITING_TAGS);
                    return new SendMessage(chatId, "Введите тэги через запятую (опционально). Введите - для пропуска")
                        .replyToMessageId(replyTo);
                }

                return new SendMessage(chatId, "Введите ссылку для отслеживания")
                    .replyToMessageId(replyTo);




            case AWAITING_TAGS:
                // Обрабатываем тэги
                if (!messageText.isEmpty()) {
                    data.tags = parseList(messageText);
                }

                userStates.put(chatId, State.AWAITING_FILTERS);
                return new SendMessage(chatId, "Настройте фильтры через запятую (опционально). Введите - для пропуска")
                    .replyToMessageId(replyTo);

            case AWAITING_FILTERS:
                // Обрабатываем фильтры
                if (!messageText.isEmpty()) {
                    data.filters = parseList(messageText);
                }

                userStates.put(chatId, State.COMPLETE);

                AddLinkRequest addLinkRequest = new AddLinkRequest(data.url, data.tags, data.filters);
                scrapperHttpClient.addLink(chatId, addLinkRequest).subscribe();

                userData.remove(chatId);
                userStates.remove(chatId);  // Завершаем диалог

                return new SendMessage(chatId, "Ссылка добавлена: " + data.url +
                    "\nТеги: " + String.join(", ", data.tags) +
                    "\nФильтры: " + String.join(", ", data.filters))
                    .replyToMessageId(replyTo);

            default:
                userStates.put(chatId, State.AWAITING_URL);
                return new SendMessage(chatId, "Введите ссылку для отслеживания")
                    .replyToMessageId(replyTo);
        }
    }



    // Метод, чтобы сразу передать URL
    public SendMessage handleUrl(Update update, String url) {
        Long chatId = update.message().chat().id();
        int replyTo = update.message().messageId();
        userStates.putIfAbsent(chatId, State.AWAITING_TAGS);
        TrackedData data = userData.computeIfAbsent(chatId, k -> new TrackedData());

        if (!isValidUrl(url)) {
            return new SendMessage(chatId, "Неверный формат ссылки. Поддерживаются только GitHub и StackOverflow.")
                .replyToMessageId(replyTo);
        }

        data.url = url;
        userStates.put(chatId, State.AWAITING_TAGS);
        return new SendMessage(chatId, "Введите тэги через запятую (опционально)")
            .replyToMessageId(replyTo);
    }

    private boolean isValidUrl(String url) {
        return url.matches("^(https?://)?(www\\.)?(github\\.com|stackoverflow\\.com)/.*$");
    }

    private List<String> parseList(String input) {
        if (input.isEmpty() || input.equals("-")) {
            return List.of();
        }
        return Arrays.asList(input.split("\\s*,\\s*"));
    }

}

package backend.academy.bot.commands.track;

import backend.academy.bot.clients.ScrapperHttpClient;
import backend.academy.bot.dto.AddLinkRequest;
import backend.academy.bot.dto.FilterDto;
import backend.academy.bot.dto.ListLinksResponse;
import backend.academy.bot.dto.TagDto;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;

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

    @ToString
    private static class TrackedData {
        String url;
        List<String> tags;
        List<String> filters;
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
                        return new SendMessage(
                                        chatId, "Неверный формат ссылки. Поддерживаются только GitHub и StackOverflow.")
                                .replyToMessageId(replyTo);
                    }

                    try {
                        ListLinksResponse linksResponse =
                                scrapperHttpClient.getLinks(chatId).block();
                        String finalMessageText = messageText;
                        if (linksResponse != null
                                && linksResponse.links().stream()
                                        .anyMatch(link -> link.url().equals(finalMessageText))) {
                            return new SendMessage(chatId, "Эта ссылка уже отслеживается.").replyToMessageId(replyTo);
                        }
                    } catch (HttpStatusCodeException e) {
                        log.error(
                                "Ошибка HTTP при получении списка ссылок для чата {}: HTTP статус {}. Сообщение: {}",
                                chatId,
                                e.getStatusCode(),
                                e.getResponseBodyAsString(),
                                e);
                        return new SendMessage(
                                        chatId,
                                        "Ошибка на сервере при получении списка ссылок. Пожалуйста, попробуйте позже.")
                                .replyToMessageId(replyTo);
                    } catch (Exception e) {
                        log.error("Неизвестная ошибка при получении списка ссылок для чата {}: ", chatId, e);
                        return new SendMessage(
                                        chatId, "Произошла ошибка при проверке ссылки. Пожалуйста, попробуйте позже.")
                                .replyToMessageId(replyTo);
                    }

                    data.url = messageText;
                    userStates.put(chatId, State.AWAITING_TAGS);
                    return new SendMessage(chatId, "Введите тэги через запятую (опционально). Введите - для пропуска")
                            .replyToMessageId(replyTo);
                }

                return new SendMessage(chatId, "Введите ссылку для отслеживания").replyToMessageId(replyTo);

            case AWAITING_TAGS:
                // Обрабатываем тэги
                if (!messageText.isEmpty()) {
                    data.tags = parseList(messageText);
                }

                userStates.put(chatId, State.AWAITING_FILTERS);
                return new SendMessage(chatId, "Настройте фильтры через запятую (опционально). Введите - для пропуска")
                        .replyToMessageId(replyTo);

            case AWAITING_FILTERS:
                if (!messageText.isEmpty()) {
                    data.filters = parseList(messageText);
                }

                userStates.put(chatId, State.COMPLETE);

                // Преобразуем теги в TagDto
                List<TagDto> tagObjects = data.tags.stream().map(TagDto::new).collect(Collectors.toList());
                List<FilterDto> filterObjects =
                        data.filters.stream().map(FilterDto::new).collect(Collectors.toList());

                AddLinkRequest addLinkRequest = new AddLinkRequest(chatId, data.url, tagObjects, filterObjects);

                try {
                    log.info(
                            "Добавление ссылки {} с тегами {} и фильтрами {} для чата {}",
                            data.url,
                            data.tags,
                            data.filters,
                            chatId);
                    scrapperHttpClient.addLink(chatId, addLinkRequest).subscribe();
                } catch (Exception e) {
                    log.error("Ошибка при добавлении ссылки {} для чата {}: ", data.url, chatId, e);
                    return new SendMessage(
                                    chatId, "Произошла ошибка при добавлении ссылки. Пожалуйста, попробуйте позже.")
                            .replyToMessageId(replyTo);
                }

                userData.remove(chatId);
                userStates.remove(chatId);

                return new SendMessage(
                                chatId,
                                "Ссылка добавлена: " + data.url + "\nТеги: "
                                        + String.join(", ", data.tags) + "\nФильтры: "
                                        + String.join(", ", data.filters))
                        .replyToMessageId(replyTo);

            default:
                userStates.put(chatId, State.AWAITING_URL);
                return new SendMessage(chatId, "Введите ссылку для отслеживания").replyToMessageId(replyTo);
        }
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

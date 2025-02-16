package backend.academy.bot.services;

import backend.academy.bot.commands.CommandHandler;
import backend.academy.bot.commands.TrackDialog;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TelegramBotServiceTest {

    private TelegramBot telegramBot;
    private TrackDialog trackDialog;
    private TelegramBotService telegramBotService;

    @BeforeEach
    void setUp() {
        telegramBot = mock(TelegramBot.class);
        trackDialog = mock(TrackDialog.class);

        // Создаём список команд (пусть будет пустой)
        List<CommandHandler> commandHandlers = List.of();
        telegramBotService = new TelegramBotService(commandHandlers, trackDialog);
    }

    @Test
    void testUnknownCommandThrowsException() {
        // Мокируем update с неизвестной командой
        Update update = mockUpdate(12345L, "/unknowncommand");

        // Проверяем, что метод выбрасывает исключение
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            telegramBotService.processUpdate(update);
        });

        // Проверяем, что исключение содержит ожидаемое сообщение
        assertEquals("Неизвестная команда: /unknowncommand", exception.getMessage());
    }




    private Update mockUpdate(Long chatId, String text) {
        Chat chat = mock(Chat.class);
        when(chat.id()).thenReturn(chatId);

        Message message = mock(Message.class);
        when(message.chat()).thenReturn(chat);
        when(message.text()).thenReturn(text);

        Update update = mock(Update.class);
        when(update.message()).thenReturn(message);
        return update;
    }
}

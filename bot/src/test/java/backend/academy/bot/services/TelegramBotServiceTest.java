package backend.academy.bot.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import backend.academy.bot.commands.CommandHandler;
import backend.academy.bot.commands.track.TrackDialog;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class TelegramBotServiceTest {

    private TelegramBot telegramBot;
    private TrackDialog trackDialog;
    private TelegramBotService telegramBotService;
    private MeterRegistry registry;

    @BeforeEach
    void setUp() {
        telegramBot = mock(TelegramBot.class);
        trackDialog = mock(TrackDialog.class);
        registry = mock(MeterRegistry.class);
        var counter = mock(io.micrometer.core.instrument.Counter.class);

        // Заглушка для любого аргумента
        when(registry.counter(any())).thenReturn(counter);

        List<CommandHandler> commandHandlers = List.of();
        telegramBotService = new TelegramBotService(commandHandlers, trackDialog, registry);
    }

    @Test
    void testUnknownCommandThrowsException() {
        Update update = mockUpdate(12345L, "/unknowncommand");

        Exception ex = assertThrows(Exception.class, () -> {
            telegramBotService.processUpdate(update);
        });

        ex.printStackTrace();

        // Временно проверим класс исключения
        System.out.println("Thrown exception: " + ex.getClass());
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

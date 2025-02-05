package backend.academy.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;


public class TelegramBotService {

    private final TelegramBot telegramBot;

    @Autowired
    public TelegramBotService(BotConfig botConfig) {
        this.telegramBot = new TelegramBot(botConfig.telegramToken());
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(new UpdatesListener() {
            @Override
            public int process(List<Update> list) {
                for (Update update : list) {

                }
                return 0;
            }
        }
        );
    }

    private void handleMessage
}

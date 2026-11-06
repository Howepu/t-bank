package backend.academy.bot.commands;

import com.pengrad.telegrambot.model.Update;

public class UpdateUtils {
    public static long getChatId(Update update) {
        return update.message().chat().id();
    }

    public static String getMessageText(Update update) {
        return update.message().text();
    }
}

package backend.academy.bot.commands;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;

public interface CommandHandler {

    String command();

    String description();

    SendMessage handle(Update update);
}

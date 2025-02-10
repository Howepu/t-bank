package backend.academy.bot.commands;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HelpCommand implements CommandHandler {

    private final List<CommandHandler> commands;

    public HelpCommand(List<CommandHandler> commands) {
        this.commands = commands;
    }

    @Override
    public String command() {
        return "/help";
    }

    @Override
    public String description() {
        return "Вывести список доступных команд";
    }

    @Override
    public SendMessage handle(Update update) {
        long chatId = update.message().chat().id();

        StringBuilder stringBuilder = new StringBuilder("Список команд:\n");
        for (CommandHandler command : commands) {
            stringBuilder.append(command.command()).append(" - ").append(command.description()).append("\n");
        }

        return new SendMessage(chatId, stringBuilder.toString());
    }
}

package backend.academy.bot.dto;

import backend.academy.bot.commands.mode.NotificationMode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SetModeRequest {
    NotificationMode mode;
}

package backend.academy.bot.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatStateData {
    private ChatState chatState = ChatState.DEFAULT;
    private Link currentEditedLink;
}

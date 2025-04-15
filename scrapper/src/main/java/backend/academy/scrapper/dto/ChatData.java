package backend.academy.scrapper.dto;

import backend.academy.scrapper.model.ChatState;
import java.util.Set;

public record ChatData(Long id, ChatState state, LinkData currentEditedLink, Set<LinkData> links) {}

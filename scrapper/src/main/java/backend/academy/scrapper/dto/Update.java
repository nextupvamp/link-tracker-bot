package backend.academy.scrapper.dto;

import backend.academy.scrapper.model.Subscription;
import lombok.Builder;

@Builder
public record Update(Subscription subscription, String topic, String username, long time, String preview) {}

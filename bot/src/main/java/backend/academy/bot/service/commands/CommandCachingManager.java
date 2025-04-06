package backend.academy.bot.service.commands;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;

@Component
public class CommandCachingManager {
    public static final String CACHE_NAME = "commands";

    @CacheEvict(key = "#chatId", value = CACHE_NAME)
    public void evictCache(long chatId) {}
}

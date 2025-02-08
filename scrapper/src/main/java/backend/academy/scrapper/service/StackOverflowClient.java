package backend.academy.scrapper.service;

import backend.academy.scrapper.model.Subscription;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.web.reactive.function.client.WebClient;

public class StackOverflowClient implements Client {
    private static final Pattern STACK_OVERFLOW_URL_REGEX =
        Pattern.compile("https://stackoverflow\\.com/questions/(?<id>[0-9]+)/.*");

    private final WebClient webClient;

    public StackOverflowClient(String stackExchangeApiUrl) {
        this.webClient = WebClient.create(stackExchangeApiUrl);
    }

    @Override
    public Optional<Update> checkUpdates(Subscription subscription) {
        Response response = webClient
            .get()
            .uri(getQuestionApiPath(subscription.url()))
            .retrieve()
            .bodyToMono(Response.class)
            .block();
        // add 4xx, 5xx status check
        long lastActivityDate = response.items()[0].lastActivityDate();
        if (lastActivityDate > subscription.lastUpdate()) {
            subscription.lastUpdate(lastActivityDate);
            return Optional.of(new Update(subscription.url(), ""));
        }
        return Optional.empty();
    }

    private String getQuestionApiPath(String questionUrl) {
        Matcher matcher = STACK_OVERFLOW_URL_REGEX.matcher(questionUrl);
        String questionId = "";
        // the check is necessary for matcher (it doesn't recognize group w/o check), but not for me
        if (matcher.matches()) {
            questionId = matcher.group("id");
        }
        return "/questions/" + questionId + "?site=stackoverflow";
    }

    record Response(Item[] items) {
    }

    record Item(@JsonProperty("last_activity_date") long lastActivityDate) {
    }
}

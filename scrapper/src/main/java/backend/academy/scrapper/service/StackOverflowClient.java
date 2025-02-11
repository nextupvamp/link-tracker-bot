package backend.academy.scrapper.service;

import backend.academy.scrapper.model.Subscription;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.web.reactive.function.client.WebClient;

public class StackOverflowClient implements Client {
    // it's used to extract question id from url
    private static final Pattern STACK_OVERFLOW_URL_REGEX =
        Pattern.compile("https://stackoverflow\\.com/questions/(?<id>[0-9]+)/.*");

    private final WebClient webClient;

    public StackOverflowClient(String stackExchangeApiUrl) {
        webClient = WebClient.create(stackExchangeApiUrl);
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
            return Optional.of(new Update(subscription, "new update"));
        }

        return Optional.empty();
    }

    private String getQuestionApiPath(String questionUrl) {
        Matcher matcher = STACK_OVERFLOW_URL_REGEX.matcher(questionUrl);

        // this check is necessary for matcher (it doesn't recognize group w/o check),
        // but not for me
        String questionId = "";
        if (matcher.matches()) {
            questionId = matcher.group("id");
        }

        return "/questions/" + questionId + "?site=stackoverflow";
    }

    // those are used to extract exact field from an api response
    private record Response(Item[] items) {
    }

    private record Item(@JsonProperty("last_activity_date") long lastActivityDate) {
    }
}

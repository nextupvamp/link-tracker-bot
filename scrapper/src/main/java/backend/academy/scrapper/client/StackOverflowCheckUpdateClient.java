package backend.academy.scrapper.client;

import backend.academy.scrapper.ScrapperConfigProperties;
import backend.academy.scrapper.dto.Update;
import backend.academy.scrapper.dto.ApiErrorResponse;
import backend.academy.scrapper.model.Subscription;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Slf4j
public class StackOverflowCheckUpdateClient implements CheckUpdateClient {
    private static final Pattern STACK_OVERFLOW_URL_REGEX =
            Pattern.compile("https://stackoverflow\\.com/questions/(?<id>[0-9]+)/.*");

    private final WebClient webClient;
    private final ScrapperConfigProperties config;

    public StackOverflowCheckUpdateClient(WebClient.Builder webClientBuilder, ScrapperConfigProperties config) {
        this.config = config;
        webClient = webClientBuilder
                .baseUrl(config.stackExchangeApiUrl())
                .filter(logRequest())
                .filter(ExchangeFilterFunction.ofResponseProcessor(this::renderApiErrorResponse))
                .filter(logResponse())
                .build();
    }

    @Override
    public Optional<Update> checkUpdates(Subscription subscription) {
        QuestionResponse questionResponse = getQuestion(subscription.url());

        if (questionResponse == null || questionResponse.questions() == null) {
            log.atInfo().setMessage("StackOverflow has sent null response").log();
            return Optional.empty();
        }

        Question question = questionResponse.questions().getFirst();

        long lastActivityDate = question.lastActivityDate();
        if (lastActivityDate > subscription.lastUpdate()) {
            subscription.lastUpdate(lastActivityDate);

            AnswerResponse answerResponse = getLastAnswer(subscription.url());
            Answer answer = answerResponse.answers().getFirst();

            return Optional.of(Update.builder()
                    .subscription(subscription)
                    .time(answer.lastEditDate())
                    .topic(question.title())
                    .preview(answer.body().substring(0, config.previewSize()))
                    .build());
        }

        return Optional.empty();
    }

    private QuestionResponse getQuestion(String url) {
        return webClient
                .get()
                .uri(getQuestionApiPath(url))
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp -> resp.bodyToMono(ApiErrorResponse.class)
                        .flatMap(error -> Mono.error(new ResponseStatusException(HttpStatus.valueOf(error.code())))))
                .bodyToMono(QuestionResponse.class)
                .block();
    }

    private AnswerResponse getLastAnswer(String url) {
        return webClient
                .get()
                .uri(getLastAnswerApiPath(url))
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp -> resp.bodyToMono(ApiErrorResponse.class)
                        .flatMap(error -> Mono.error(new ResponseStatusException(HttpStatus.valueOf(error.code())))))
                .bodyToMono(AnswerResponse.class)
                .block();
    }

    private String getQuestionApiPath(String questionUrl) {
        return "/questions/" + getQuestionId(questionUrl) + "?site=stackoverflow";
    }

    private String getLastAnswerApiPath(String questionUrl) {
        return "/questions/" + getQuestionId(questionUrl)
                + "answers?pagesize=1&order=desc&sort=activity&site=stackoverflow&filter=!nNPvSNe7Gv";
    }

    private String getQuestionId(String questionUrl) {
        Matcher matcher = STACK_OVERFLOW_URL_REGEX.matcher(questionUrl);

        String questionId = "";
        if (matcher.matches()) {
            questionId = matcher.group("id");
        }

        return questionId;
    }

    private Mono<ClientResponse> renderApiErrorResponse(ClientResponse clientResponse) {
        return ClientUtils.renderApiErrorResponse(clientResponse, log);
    }

    private ExchangeFilterFunction logRequest() {
        return ClientUtils.logRequest(log);
    }

    private ExchangeFilterFunction logResponse() {
        return ClientUtils.logResponse(log);
    }

    private record QuestionResponse(List<Question> questions) {}

    private record Question(String title, @JsonProperty("last_activity_date") long lastActivityDate) {}

    private record AnswerResponse(List<Answer> answers) {}

    private record Answer(Owner owner, @JsonProperty("last_edit_date") long lastEditDate, String body) {}

    private record Owner(@JsonProperty("display_name") String username) {}
}

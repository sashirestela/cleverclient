package io.github.sashirestela.cleverclient.example;

import io.github.sashirestela.cleverclient.CleverClient;
import io.github.sashirestela.cleverclient.example.openai.ChatRequest;
import io.github.sashirestela.cleverclient.example.openai.ChatResponse;
import io.github.sashirestela.cleverclient.example.openai.ChatService;
import io.github.sashirestela.cleverclient.example.openai.Message;
import io.github.sashirestela.cleverclient.example.util.Commons;
import io.github.sashirestela.cleverclient.example.util.Commons.MyExceptionConverter;
import io.github.sashirestela.cleverclient.example.util.Commons.MyHttpException;
import io.github.sashirestela.cleverclient.retry.RetryConfig;

import java.util.Arrays;

/**
 * Before running this example you must have an OpenAI account and keep your Api Key in an
 * environment variable called OPENAI_API_KEY.
 * 
 * @see <a href= "https://platform.openai.com/docs/api-reference/authentication">OpenAI
 *      Authentication</a>
 */
public class StreamExample extends AbstractExample {

    public StreamExample(String clientAlias) {
        super(clientAlias);
    }

    public StreamExample() {
        this("javahttp");
    }

    public void run() {
        try {
            Commons.redirectSystemErr();

            var cleverClient = CleverClient.builder()
                    .baseUrl("https://api.openai.com")
                    .clientAdapter(clientAdapter)
                    .header("Authorization", "Bearer " + System.getenv("OPENAI_API_KEY"))
                    .retryConfig(RetryConfig.defaultValues())
                    .endOfStream("[DONE]")
                    .build();
            var chatService = cleverClient.create(ChatService.class);

            var chatRequest = ChatRequest.builder()
                    .model("gpt-3.5-turbo")
                    .messages(Arrays.asList(
                            new Message("user", "Write an article about AI, no more than 100 words.")))
                    .temperature(0.7)
                    .build();

            showTitle("Example Create Synchronous Stream");
            var chatResponseSync = chatService.createSyncStream(chatRequest);
            chatResponseSync
                    .filter(chatResp -> chatResp.firstContent() != null)
                    .map(ChatResponse::firstContent)
                    .forEach(System.out::print);
            System.out.println();

            showTitle("Example Create Asynchronous Stream");
            var chatResponseAsync = chatService.createAsyncStream(chatRequest).join();
            chatResponseAsync
                    .filter(chatResp -> chatResp.firstContent() != null)
                    .map(ChatResponse::firstContent)
                    .forEach(System.out::print);
            System.out.println();

            clientAdapter.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                MyExceptionConverter.rethrow(e);
            } catch (MyHttpException mhe) {
                System.out.println("Http Response Code: " + mhe.getResponseCode() +
                        "\nError Detail:\n" + mhe.getErrorDetail());
            } catch (RuntimeException re) {
                System.out.println(re.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        var example = new StreamExample();
        example.run();
    }

}

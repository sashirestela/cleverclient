package io.github.sashirestela.cleverclient.example;

import io.github.sashirestela.cleverclient.CleverClient;
import io.github.sashirestela.cleverclient.ExceptionConverter;
import io.github.sashirestela.cleverclient.ResponseInfo;
import io.github.sashirestela.cleverclient.example.openai.ChatRequest;
import io.github.sashirestela.cleverclient.example.openai.ChatResponse;
import io.github.sashirestela.cleverclient.example.openai.ChatService;
import io.github.sashirestela.cleverclient.example.openai.Message;
import lombok.Getter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Arrays;

/**
 * Before running this example you must have an OpenAI account and keep your Api Key in an
 * environment variable called OPENAI_API_KEY.
 * 
 * @see <a href= "https://platform.openai.com/docs/api-reference/authentication">OpenAI
 *      Authentication</a>
 */
public class StreamExample {

    public static void main(String[] args) throws FileNotFoundException {
        try {
            redirectSystemErr();

            final var BASE_URL = "https://api.openai.com";
            final var AUTHORIZATION_HEADER = "Authorization";
            final var BEARER_AUTHORIZATION = "Bearer " + System.getenv("OPENAI_API_KEY");
            final var END_OF_STREAM = "[DONE]";

            var cleverClient = CleverClient.builder()
                    .baseUrl(BASE_URL)
                    .header(AUTHORIZATION_HEADER, BEARER_AUTHORIZATION)
                    .endOfStream(END_OF_STREAM)
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
        } catch (Exception e) {
            try {
                MyExceptionConverter.rethrow(e);
            } catch (MyHttpException mhe) {
                System.out.println("Http Response Code: " + mhe.getResponseCode() +
                        "\nError Detail:\n" + mhe.getErrorDetail());
            } catch (RuntimeException re) {
                System.out.println(re.getMessage());
                re.printStackTrace();
            }
        }
    }

    private static void redirectSystemErr() throws FileNotFoundException {
        File file = new File("error.log");
        FileOutputStream fos = new FileOutputStream(file);
        PrintStream ps = new PrintStream(fos);
        System.setErr(ps);
    }

    private static void showTitle(String title) {
        final var times = 50;
        System.out.println("=".repeat(times));
        System.out.println(title);
        System.out.println("-".repeat(times));
    }

    public static class MyExceptionConverter extends ExceptionConverter {

        private MyExceptionConverter() {
        }

        public static void rethrow(Throwable e) {
            throw new MyExceptionConverter().convert(e);
        }

        @Override
        public RuntimeException convertHttpException(ResponseInfo responseInfo) {
            return new MyHttpException(responseInfo.getStatusCode(), responseInfo.getData());
        }

    }

    @Getter
    public static class MyHttpException extends RuntimeException {

        private final int responseCode;
        private final String errorDetail;

        public MyHttpException(int responseCode, String errorDetail) {
            this.responseCode = responseCode;
            this.errorDetail = errorDetail;
        }

    }

}

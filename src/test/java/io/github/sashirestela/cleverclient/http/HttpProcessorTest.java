package io.github.sashirestela.cleverclient.http;

import io.github.sashirestela.cleverclient.support.CleverClientException;
import io.github.sashirestela.cleverclient.test.TestSupport;
import io.github.sashirestela.cleverclient.test.TestSupport.SyncType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

interface HttpProcessorTest {

    HttpProcessor getHttpProcessor();

    HttpProcessor getHttpProcessor(UnaryOperator<HttpResponseData> responseInterceptor);

    void setMocksForString(SyncType syncType, String result) throws IOException, InterruptedException;

    void setMocksForBinary(SyncType syncType, InputStream result) throws IOException, InterruptedException;

    void setMocksForStream(SyncType syncType, Stream<String> result) throws IOException, InterruptedException;

    void setMocksForException() throws IOException, InterruptedException;

    void setMocksForStringWithError(String result) throws IOException, URISyntaxException;

    void setMocksForBinaryWithError(InputStream result) throws IOException, URISyntaxException;

    void setMocksForStreamWithError(Stream<String> result) throws IOException, URISyntaxException;

    void setMocksForInterceptor(String result) throws IOException, InterruptedException, URISyntaxException;

    void testShutdown();

    @BeforeAll
    static void setup() {
        TestSupport.setupConfigurator();
    }

    @Test
    default void shouldThownExceptionWhenCallingMethodReturnTypeIsUnsupported() {
        var service = getHttpProcessor().createProxy(ITest.AsyncService.class);
        Exception exception = assertThrows(CleverClientException.class,
                () -> service.unsupportedMethod());
        assertTrue(exception.getMessage().contains("Unsupported return type"));
    }

    @Test
    default void shouldReturnAStringSyncWhenMethodReturnTypeIsAString() throws IOException, InterruptedException {
        setMocksForString(SyncType.SYNC, "{\"id\":100,\"description\":\"Description\",\"active\":true}");

        var service = getHttpProcessor().createProxy(ITest.SyncService.class);
        var actualDemo = service.getDemoPlain(100);
        var expectedDemo = "{\"id\":100,\"description\":\"Description\",\"active\":true}";

        assertEquals(expectedDemo, actualDemo);
    }

    @Test
    default void shouldThrownExceptionWhenMethodReturnTypeIsAString() throws IOException, InterruptedException {
        setMocksForException();
        var service = getHttpProcessor().createProxy(ITest.SyncService.class);
        assertThrows(CleverClientException.class, () -> service.getDemoPlain(100));
    }

    @Test
    default void shouldshouldReturnABinarySyncWhenMethodReturnTypeIsABinary() throws IOException, InterruptedException {
        InputStream binaryData = new FileInputStream(new File("src/test/resources/image.png"));
        setMocksForBinary(SyncType.SYNC, binaryData);

        var service = getHttpProcessor().createProxy(ITest.SyncService.class);
        var actualDemo = service.getDemoBinary(100);
        var expectedDemo = binaryData;

        assertEquals(expectedDemo, actualDemo);
    }

    @Test
    default void shouldThrownExceptionWhenMethodReturnTypeIsABinary() throws IOException, InterruptedException {
        setMocksForException();
        var service = getHttpProcessor().createProxy(ITest.SyncService.class);
        assertThrows(CleverClientException.class, () -> service.getDemoBinary(100));
    }

    @Test
    default void shouldReturnAnObjectSyncWhenMethodReturnTypeIsAnObject() throws IOException, InterruptedException {
        setMocksForString(SyncType.SYNC, "{\"id\":100,\"description\":\"Description\",\"active\":true}");

        var service = getHttpProcessor().createProxy(ITest.SyncService.class);
        var actualDemo = service.getDemo(100);
        var expectedDemo = new ITest.Demo(100, "Description", true);

        assertEquals(expectedDemo, actualDemo);
    }

    @Test
    default void shouldThrownExceptionWhenMethodReturnTypeIsAnObject() throws IOException, InterruptedException {
        setMocksForException();
        var service = getHttpProcessor().createProxy(ITest.SyncService.class);
        assertThrows(CleverClientException.class, () -> service.getDemo(100));
    }

    @Test
    default void shouldReturnAGenericSyncWhenMethodReturnTypeIsAnObject() throws IOException, InterruptedException {
        setMocksForString(SyncType.SYNC,
                "{\"id\":1,\"listDemo\":[{\"id\":100,\"description\":\"Description\",\"active\":true}]}");

        var service = getHttpProcessor().createProxy(ITest.SyncService.class);
        var actualGenericDemo = service.getGenericDemo(1);
        var actualDemo = actualGenericDemo.getListDemo().get(0);
        var expectedDemo = new ITest.Demo(100, "Description", true);

        assertEquals(expectedDemo, actualDemo);
    }

    @Test
    default void shouldThrownExceptionWhenMethodReturnTypeIsAGenericObject() throws IOException, InterruptedException {
        setMocksForException();
        var service = getHttpProcessor().createProxy(ITest.SyncService.class);
        assertThrows(CleverClientException.class, () -> service.getGenericDemo(1));
    }

    @Test
    default void shouldReturnAListSyncWhenMethodReturnTypeIsAList() throws IOException, InterruptedException {
        setMocksForString(SyncType.SYNC, "[{\"id\":100,\"description\":\"Description\",\"active\":true}]");

        var service = getHttpProcessor().createProxy(ITest.SyncService.class);
        var actualListDemo = service.getDemos();
        var actualDemo = actualListDemo.get(0);
        var expectedDemo = new ITest.Demo(100, "Description", true);

        assertEquals(expectedDemo, actualDemo);
    }

    @Test
    default void shouldThrownExceptionWhenMethodReturnTypeIsAList() throws IOException, InterruptedException {
        setMocksForException();
        var service = getHttpProcessor().createProxy(ITest.SyncService.class);
        assertThrows(CleverClientException.class, () -> service.getDemos());
    }

    @Test
    default void shouldReturnAStreamSyncWhenMethodReturnTypeIsAStream() throws IOException, InterruptedException {
        setMocksForStream(SyncType.SYNC,
                Stream.of("data: {\"id\":100,\"description\":\"Description\",\"active\":true}"));

        var service = getHttpProcessor().createProxy(ITest.SyncService.class);
        var actualStreamDemo = service.getDemoStream(new ITest.RequestDemo("Descr", null));
        var actualDemo = actualStreamDemo.findFirst().get();
        var expectedDemo = new ITest.Demo(100, "Description", true);

        assertEquals(expectedDemo, actualDemo);
    }

    @Test
    default void shouldThrownExceptionWhenMethodReturnTypeIsAStream() throws IOException, InterruptedException {
        setMocksForException();
        var service = getHttpProcessor().createProxy(ITest.SyncService.class);
        var requestDemo = new ITest.RequestDemo("Descr", null);
        assertThrows(CleverClientException.class, () -> service.getDemoStream(requestDemo));
    }

    @Test
    default void shouldReturnAStreamSyncWhenMethodReturnTypeIsAStreamEvent() throws IOException, InterruptedException {
        setMocksForStream(SyncType.SYNC,
                Stream.of("event: created", "data: {\"id\":100,\"description\":\"Description\",\"active\":true}"));

        var service = getHttpProcessor().createProxy(ITest.SyncService.class);
        var actualStreamObject = service.getStreamEvent(new ITest.RequestDemo("Descr", null));
        var actualObject = actualStreamObject.findFirst().get().getData();
        var expectedObject = new ITest.Demo(100, "Description", true);

        assertEquals(expectedObject, actualObject);
    }

    @Test
    default void shouldThrownExceptionWhenMethodReturnTypeIsAStreamObject() throws IOException, InterruptedException {
        setMocksForException();
        var service = getHttpProcessor().createProxy(ITest.SyncService.class);
        var requestDemo = new ITest.RequestDemo("Descr", null);
        assertThrows(CleverClientException.class, () -> service.getStreamEvent(requestDemo));
    }

    @Test
    default void shouldReturnAStringAsyncWhenMethodReturnTypeIsAString() throws IOException, InterruptedException {
        setMocksForString(SyncType.ASYNC, "{\"id\":100,\"description\":\"Description\",\"active\":true}");

        var service = getHttpProcessor().createProxy(ITest.AsyncService.class);
        var actualDemo = service.getDemoPlain(100).join();
        var expectedDemo = "{\"id\":100,\"description\":\"Description\",\"active\":true}";

        assertEquals(expectedDemo, actualDemo);
    }

    @Test
    default void shouldReturnABinaryAsyncWhenMethodReturnTypeIsABinary() throws IOException, InterruptedException {
        InputStream binaryData = new FileInputStream(new File("src/test/resources/image.png"));
        setMocksForBinary(SyncType.ASYNC, binaryData);

        var service = getHttpProcessor().createProxy(ITest.AsyncService.class);
        var actualDemo = service.getDemoBinary(100).join();
        var expectedDemo = binaryData;

        assertEquals(expectedDemo, actualDemo);
    }

    @Test
    default void shouldReturnAnObjectAsyncWhenMethodReturnTypeIsAnObject() throws IOException, InterruptedException {
        setMocksForString(SyncType.ASYNC, "{\"id\":100,\"description\":\"Description\",\"active\":true}");

        var service = getHttpProcessor().createProxy(ITest.AsyncService.class);
        var actualDemo = service.getDemo(100).join();
        var expectedDemo = new ITest.Demo(100, "Description", true);

        assertEquals(expectedDemo, actualDemo);
    }

    @Test
    default void shouldReturnAGenericAsyncWhenMethodReturnTypeIsAnObject() throws IOException, InterruptedException {
        setMocksForString(SyncType.ASYNC,
                "{\"id\":1,\"listDemo\":[{\"id\":100,\"description\":\"Description\",\"active\":true}]}");

        var service = getHttpProcessor().createProxy(ITest.AsyncService.class);
        var actualGenericDemo = service.getGenericDemo(1).join();
        var actualDemo = actualGenericDemo.getListDemo().get(0);
        var expectedDemo = new ITest.Demo(100, "Description", true);

        assertEquals(expectedDemo, actualDemo);
    }

    @Test
    default void shouldReturnAListAsyncWhenMethodReturnTypeIsAList() throws IOException, InterruptedException {
        setMocksForString(SyncType.ASYNC, "[{\"id\":100,\"description\":\"Description\",\"active\":true}]");

        var service = getHttpProcessor().createProxy(ITest.AsyncService.class);
        var actualListDemo = service.getDemos().join();
        var actualDemo = actualListDemo.get(0);
        var expectedDemo = new ITest.Demo(100, "Description", true);

        assertEquals(expectedDemo, actualDemo);
    }

    @Test
    default void shouldReturnAStreamAsyncWhenMethodReturnTypeIsAStream() throws IOException, InterruptedException {
        setMocksForStream(SyncType.ASYNC,
                Stream.of("data: {\"id\":100,\"description\":\"Description\",\"active\":true}"));

        var service = getHttpProcessor().createProxy(ITest.AsyncService.class);
        var actualStreamDemo = service.getDemoStream(new ITest.RequestDemo("Descr", null)).join();
        var actualDemo = actualStreamDemo.findFirst().get();
        var expectedDemo = new ITest.Demo(100, "Description", true);

        assertEquals(expectedDemo, actualDemo);
    }

    @Test
    default void shouldReturnAStreamAsyncWhenMethodReturnTypeIsAStreamEvent() throws IOException, InterruptedException {
        setMocksForStream(SyncType.ASYNC,
                Stream.of("event: created", "data: {\"id\":100,\"description\":\"Description\",\"active\":true}"));

        var service = getHttpProcessor().createProxy(ITest.AsyncService.class);
        var actualStreamObject = service.getStreamEvent(new ITest.RequestDemo("Descr", null)).join();
        var actualObject = actualStreamObject.findFirst().get().getData();
        var expectedObject = new ITest.Demo(100, "Description", true);

        assertEquals(expectedObject, actualObject);
    }

    @Test
    default void shouldReturnAnObjectWhenMethodIsAnnotatedWithMultipart() throws IOException, InterruptedException {
        setMocksForString(SyncType.ASYNC, "{\"id\":100,\"description\":\"Description\",\"active\":true}");

        var service = getHttpProcessor().createProxy(ITest.AsyncService.class);
        var actualDemo = service.getFile(new ITest.RequestDemo("Descr", Paths.get("src/test/resources/image.png")))
                .join();
        var expectedDemo = new ITest.Demo(100, "Description", true);

        assertEquals(expectedDemo, actualDemo);
    }

    @Test
    default void shouldThrownExceptionWhenCallingNoStreamingMethodAndServerRespondsWithError()
            throws IOException, URISyntaxException {
        setMocksForStringWithError(
                "{\"error\": {\"message\": \"The resource does not exist\", \"type\": \"T\", \"param\": \"P\", \"code\": \"C\"}}");

        var service = getHttpProcessor().createProxy(ITest.AsyncService.class);
        var futureService = service.getDemo(100);

        Exception exception = assertThrows(CompletionException.class, () -> futureService.join());
        CleverClientException nestedException = (CleverClientException) exception.getCause();
        assertNotNull(nestedException);
        assertEquals(CleverClientException.class, nestedException.getClass());
        assertTrue(nestedException.responseInfo().get().getData().contains("The resource does not exist"));
    }

    @Test
    default void shouldThrownExceptionWhenCallingBinaryMethodAndServerRespondsWithError()
            throws IOException, URISyntaxException {
        setMocksForBinaryWithError(new ByteArrayInputStream(
                "{\"error\": {\"message\": \"The resource does not exist\", \"type\": \"T\", \"param\": \"P\", \"code\": \"C\"}}"
                        .getBytes()));

        var service = getHttpProcessor().createProxy(ITest.AsyncService.class);
        var futureService = service.getDemoBinary(100);

        Exception exception = assertThrows(CompletionException.class, () -> futureService.join());
        CleverClientException nestedException = (CleverClientException) exception.getCause();
        assertNotNull(nestedException);
        assertEquals(CleverClientException.class, nestedException.getClass());
        assertTrue(nestedException.responseInfo().get().getData().contains("The resource does not exist"));
    }

    @Test
    default void shouldThrownExceptionWhenCallingStreamingMethodAndServerRespondsWithError()
            throws IOException, URISyntaxException {
        setMocksForStreamWithError(Stream.of(
                "{\"error\": {\"message\": \"The resource does not exist\", \"type\": \"T\", \"param\": \"P\", \"code\": \"C\"}}"));

        var service = getHttpProcessor().createProxy(ITest.AsyncService.class);
        var futureService = service.getDemoStream(new ITest.RequestDemo("Descr", null));

        Exception exception = assertThrows(CompletionException.class, () -> futureService.join());
        CleverClientException nestedException = (CleverClientException) exception.getCause();
        assertNotNull(nestedException);
        assertEquals(CleverClientException.class, nestedException.getClass());
        assertTrue(nestedException.responseInfo().get().getData().contains("The resource does not exist"));
    }

    @Test
    default void shouldModifyAsyncResponseWhenPassingInterceptor()
            throws IOException, InterruptedException, URISyntaxException {
        var originalJsonResponse = Files.readString(Path.of("src/test/resources/users.json"), StandardCharsets.UTF_8);
        setMocksForInterceptor(originalJsonResponse);

        var service = getHttpProcessor(response -> {
            var body = response.getBody();
            var newBody = transformUsers(body);
            response.setBody(newBody);
            return response;
        }).createProxy(ITest.UserService.class);
        var actualUserList = service.getAsyncUsers().join();
        var actualUser = actualUserList.get(0);
        var expectedUser = ITest.User.builder()
                .id(1)
                .name("Leanne Graham")
                .username("Bret")
                .email("Sincere@april.biz")
                .address("Kulas Light, Apt. 556, Gwenborough")
                .phone("1-770-736-8031 x56442")
                .website("hildegard.org")
                .company("Romaguera-Crona")
                .build();

        assertEquals(expectedUser, actualUser);
    }

    @Test
    default void shouldExecuteDefaultMethodWhenItIsCalled() {
        var service = getHttpProcessor().createProxy(ITest.AsyncService.class);
        var actualValue = service.defaultMethod("Test");
        var expectedValue = "Hello Test";

        assertEquals(expectedValue, actualValue);
    }

    @Test
    default void shouldShutdownWithoutExceptions() {
        testShutdown();
    }

    @Test
    default void shouldReturnAnObjectSyncWhenIsPostMethodAndBodyIsEmpty() throws IOException, InterruptedException {
        setMocksForString(SyncType.SYNC, "{\"id\":100,\"description\":\"Description\",\"active\":true}");

        var service = getHttpProcessor().createProxy(ITest.SyncService.class);
        var actualDemo = service.cancelDemo(100);
        var expectedDemo = new ITest.Demo(100, "Description", true);

        assertEquals(expectedDemo, actualDemo);
    }

    @Test
    default void shouldReturnAnObjectAsyncWhenIsPostMethodAndBodyIsEmpty() throws IOException, InterruptedException {
        setMocksForString(SyncType.ASYNC, "{\"id\":100,\"description\":\"Description\",\"active\":true}");

        var service = getHttpProcessor().createProxy(ITest.AsyncService.class);
        var actualDemo = service.cancelDemo(100).join();
        var expectedDemo = new ITest.Demo(100, "Description", true);

        assertEquals(expectedDemo, actualDemo);
    }

    private String transformUsers(String jsonInput) {
        List<String> flatUsers = new ArrayList<>();
        String patternStr = "\"id\":\\s*(\\d+).*?" + // id
                "\"name\":\\s*\"([^\"]+)\".*?" + // name
                "\"username\":\\s*\"([^\"]+)\".*?" + // username
                "\"email\":\\s*\"([^\"]+)\".*?" + // email
                "\"street\":\\s*\"([^\"]+)\".*?" + // street
                "\"suite\":\\s*\"([^\"]+)\".*?" + // suite
                "\"city\":\\s*\"([^\"]+)\".*?" + // city
                "\"phone\":\\s*\"([^\"]+)\".*?" + // phone
                "\"website\":\\s*\"([^\"]+)\".*?" + // website
                "\"company\":\\s*\\{\\s*\"name\":\\s*\"([^\"]+)\""; // company name
        Pattern pattern = Pattern.compile(patternStr, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(jsonInput);
        while (matcher.find()) {
            String flatUser = String.format(
                    "{\n" +
                            "  \"id\": %s,\n" +
                            "  \"name\": \"%s\",\n" +
                            "  \"username\": \"%s\",\n" +
                            "  \"email\": \"%s\",\n" +
                            "  \"address\": \"%s, %s, %s\",\n" +
                            "  \"phone\": \"%s\",\n" +
                            "  \"website\": \"%s\",\n" +
                            "  \"company\": \"%s\"\n" +
                            "}",
                    matcher.group(1), // id
                    matcher.group(2), // name
                    matcher.group(3), // username
                    matcher.group(4), // email
                    matcher.group(5), // street
                    matcher.group(6), // suite
                    matcher.group(7), // city
                    matcher.group(8), // phone
                    matcher.group(9), // website
                    matcher.group(10) // company name
            );
            flatUsers.add(flatUser);
        }
        if (flatUsers.isEmpty()) {
            System.err.println("No matches found in input: " + jsonInput);
            return "[]";
        }
        return "[\n  " + String.join(",\n  ", flatUsers) + "\n]";
    }

}

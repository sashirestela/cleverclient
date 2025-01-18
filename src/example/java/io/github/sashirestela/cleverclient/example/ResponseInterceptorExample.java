package io.github.sashirestela.cleverclient.example;

import io.github.sashirestela.cleverclient.CleverClient;
import io.github.sashirestela.cleverclient.example.jsonplaceholder.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResponseInterceptorExample extends AbstractExample {

    public ResponseInterceptorExample(String clientAlias) {
        super(clientAlias);
    }

    public ResponseInterceptorExample() {
        this("javahttp");
    }

    public void run() {
        var cleverClient = CleverClient.builder()
                .baseUrl("https://jsonplaceholder.typicode.com")
                .responseInterceptor(response -> {
                    var body = response.getBody();
                    var newBody = transformUsers(body);
                    response.setBody(newBody);
                    return response;
                })
                .clientAdapter(clientAdapter)
                .build();
        var userService = cleverClient.create(UserService.class);

        showTitle("Example Read Users");
        var usersList = userService.readUsers(1, 5);
        usersList.forEach(System.out::println);
    }

    private String transformUsers(String jsonInput) {
        List<String> flatUsers = new ArrayList<>();

        // Simpler pattern that matches each field individually
        String patternStr = "\"id\":\\s*(\\d+).*?" +              // id
                "\"name\":\\s*\"([^\"]+)\".*?" +      // name
                "\"username\":\\s*\"([^\"]+)\".*?" +   // username
                "\"email\":\\s*\"([^\"]+)\".*?" +      // email
                "\"street\":\\s*\"([^\"]+)\".*?" +     // street
                "\"suite\":\\s*\"([^\"]+)\".*?" +      // suite
                "\"city\":\\s*\"([^\"]+)\".*?" +       // city
                "\"phone\":\\s*\"([^\"]+)\".*?" +      // phone
                "\"website\":\\s*\"([^\"]+)\".*?" +    // website
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
                    matcher.group(1),  // id
                    matcher.group(2),  // name
                    matcher.group(3),  // username
                    matcher.group(4),  // email
                    matcher.group(5),  // street
                    matcher.group(6),  // suite
                    matcher.group(7),  // city
                    matcher.group(8),  // phone
                    matcher.group(9),  // website
                    matcher.group(10)  // company name
            );
            flatUsers.add(flatUser);
        }

        // If no matches were found, print input for debugging
        if (flatUsers.isEmpty()) {
            System.err.println("No matches found in input: " + jsonInput);
            return "[]";
        }

        // Combine all users into a JSON array
        return "[\n  " + String.join(",\n  ", flatUsers) + "\n]";
    }

    public static void main(String[] args) {
        var example = new ResponseInterceptorExample();
        example.run();
    }

}

package io.github.sashirestela.cleverclient.support;

import io.github.sashirestela.cleverclient.http.ITest;
import io.github.sashirestela.cleverclient.util.JsonUtil;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpMultipartTest {

    @Test
    void testToByteArrays() {
        String[] expectedData = {
                "Content-Disposition: form-data",
                "Content-Type: text/plain\r\n",
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
                "Content-Disposition: form-data",
                "; name=\"id\"\r\n",
                "101\r\n",
                "Content-Disposition: form-data",
                "; name=\"text\"\r\n",
                "Testing\r\n",
                "Content-Disposition: form-data",
                "; name=\"group[]\"\r\n",
                "one\r\n",
                "Content-Disposition: form-data",
                "; name=\"group[]\"\r\n",
                "two\r\n",
                "Content-Disposition: form-data",
                "; name=\"numbers[]\"\r\n",
                "13\r\n",
                "Content-Disposition: form-data",
                "; name=\"numbers[]\"\r\n",
                "25\r\n",
                "Content-Disposition: form-data",
                "; name=\"numbers[]\"\r\n",
                "37\r\n"
        };
        var object = new ITest.MultipartClass(Paths.get("src/test/resources/loremipsum.txt"), 101, "Testing",
                List.of("one", "two"), new Integer[] { 13, 25, 37 });
        var objectMap = JsonUtil.objectToMap(object);
        var bytesList = HttpMultipart.toByteArrays(objectMap);
        var i = 0;
        for (byte[] bytes : bytesList) {
            var currentData = new String(bytes);
            if (!currentData.startsWith("--") && !currentData.startsWith("\r\n")
                    && !currentData.contains("filename=")) {
                assertEquals(expectedData[i], currentData);
                i++;
            }
        }
    }

}

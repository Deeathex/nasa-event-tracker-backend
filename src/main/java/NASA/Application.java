package NASA;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class Application {
    private static final String SWAGGER_UI = "http://localhost:8080/nasa-natural-event-tracker/swagger-ui.html";

    public static void main(String[] args) throws IOException {
        SpringApplication.run(Application.class, args);
        openHomePage();
    }

    private static void openHomePage() throws IOException {
        Runtime rt = Runtime.getRuntime();
        rt.exec("rundll32 url.dll,FileProtocolHandler" + SWAGGER_UI);
    }
}

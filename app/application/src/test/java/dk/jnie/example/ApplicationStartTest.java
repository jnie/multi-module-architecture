package dk.jnie.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Application Startup Test")
class ApplicationStartTest {

    @Test
    @DisplayName("Application starts successfully")
    void contextLoads() {
    }
}
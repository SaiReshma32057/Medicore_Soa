package com.medicore.eureka;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EurekaServerApplicationTests {
    @Test void applicationManifestIsPresent() { assertTrue(EurekaServerApplication.class.isAnnotationPresent(org.springframework.boot.autoconfigure.SpringBootApplication.class)); }
}

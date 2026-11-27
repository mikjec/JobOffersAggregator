package pl.pollub.agregator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.pollub.agregator.entity.Role;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AgregatorApplicationTests {

    @Test
    @DisplayName("Verify main application class and system roles")
    void contextLoads() {
        AgregatorApplication application = new AgregatorApplication();
        assertNotNull(application);
        assertEquals(3, Role.values().length);
    }
}

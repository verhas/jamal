package javax0.jamal.api;

import javax0.jamal.api.services.Service;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestServiceLoaded {

    @Test
    @DisplayName("ddd")
    void testUsualService(){
        this.getClass().getModule().addUses(Service.class);
        final var services = Service.getInstances();
        Assertions.assertEquals(2,services.size());
    }

}

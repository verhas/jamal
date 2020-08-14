package javax0.jamal.maven;

import org.junit.jupiter.api.Test;

public class TestJamalMojo {

    private JamalMojo sut;

    private void set(String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        var field = JamalMojo.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(sut, value);
    }

    @Test
    public void testNothing() {
        sut = new JamalMojo();
//        sut.execute();
    }

}

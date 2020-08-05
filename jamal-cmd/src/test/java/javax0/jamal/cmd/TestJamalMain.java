package javax0.jamal.cmd;

import org.junit.jupiter.api.Test;

public class TestJamalMain {

    private JamalMain sut;

    private void set(String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        var field = JamalMain.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(sut, value);
    }

    @Test
    public void testNothing() {
        sut = new JamalMain();
//        sut.execute();
    }

}

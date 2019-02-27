package javax0.jamal.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestJamalMojo {

    private void set(String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        var field = JamalMojo.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(sut,value);
    }

        private JamalMojo sut;

    @Test
    public void testNothing() throws MojoExecutionException, NoSuchFieldException, IllegalAccessException {
        sut = new JamalMojo();
//        sut.execute();
    }

}

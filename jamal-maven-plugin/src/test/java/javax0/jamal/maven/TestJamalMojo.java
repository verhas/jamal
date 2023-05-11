package javax0.jamal.maven;

import com.javax0.jamal.maven.JamalMojo;
import com.javax0.jamal.maven.Jamalizer;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.jupiter.api.Test;

public class TestJamalMojo {

    private JamalMojo sut;

    private void set(String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        var field = JamalMojo.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(sut, value);
    }

    @Test
    public void testNothing() throws MojoExecutionException, MojoFailureException {
        final var sut = new Jamalizer();
        sut.execute();
    }

}

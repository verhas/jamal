package javax0.jamal.test.core;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

public class TestUserDefined {

    @Test
    void testDefault() throws Exception {
        TestThat.theInput("{@define default=wupppps...}{something}").results("wupppps...");
    }
    @Test
    void testDefaultOptional() throws Exception {
        TestThat.theInput("{@define default=wupppps...}{?something}").results("wupppps...");
    }
}

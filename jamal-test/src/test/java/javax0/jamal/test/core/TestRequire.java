package javax0.jamal.test.core;

import javax0.jamal.api.Processor;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestRequire {

    @Test
    @DisplayName("Test that the 'require' macro throws an exception if there are no arguments")
    void testRequireEmptyStringThrowsException() throws Exception {
        TestThat.theInput("{@require}").throwsBadSyntax();
    }

    @Test
    @DisplayName("Test that the 'require' macro throws an exception if there is a unusable comparison")
    void testRequireEmptyBadComparisonThrowsException() throws Exception {
        TestThat.theInput("{@require != 1.6.7}").throwsBadSyntax();
    }

    @Test
    @DisplayName("Test that the 'require' macro throws an exception if there is no version after the comparison sign")
    void testRequireWoVersionThrowsException() throws Exception {
        TestThat.theInput("{@require <}").throwsBadSyntax();
        TestThat.theInput("{@require <=}").throwsBadSyntax();
        TestThat.theInput("{@require >=}").throwsBadSyntax();
        TestThat.theInput("{@require >}").throwsBadSyntax();
        TestThat.theInput("{@require =}").throwsBadSyntax();
    }

    @Test
    @DisplayName("Test that requiring an old version throws BadSyntax")
    void testOldVersion() throws Exception {
        TestThat.theInput("{@require 1.5.3}").throwsBadSyntax();
    }

    @Test
    @DisplayName("Test that requiring the latest \"old\" version throws BadSyntax")
    void testLatestOldVersion() throws Exception {
        TestThat.theInput("{@require 1.6.3}").throwsBadSyntax();
    }

    @Test
    @DisplayName("Test that requiring the first \"new\" version is okay")
    void testFirstNewVersion() throws Exception {
        TestThat.theInput("{@require 1.6.4-SNAPSHOT}").results("");
    }

    @Test
    @DisplayName("Test that requiring a future version throws exception")
    void testFutureVersion() throws Exception {
        final var current = Processor.jamalVersion();
        final var futureF = (current.feature() + 1) + "." + current.interim() + "." + current.update();
        TestThat.theInput("{@require " + futureF + "}").throwsBadSyntax();
        final var futureI = current.feature() + "." + (current.interim() + 1) + "." + current.update();
        TestThat.theInput("{@require " + futureI + "}").throwsBadSyntax();
        final var futureU = current.feature() + "." + current.interim() + "." + (current.update() + 1);
        TestThat.theInput("{@require " + futureU + "}").throwsBadSyntax();
    }

    @Test
    @DisplayName("Test that requiring the exact current version is okay")
    void testCurrentExactVersion() throws Exception {
        final var current = Processor.jamalVersion();
        TestThat.theInput("{@require =" + current + "}").results("");
        TestThat.theInput("{@require <=" + current + "}").results("");
        TestThat.theInput("{@require >=" + current + "}").results("");
        TestThat.theInput("{@require >" + current + "}").throwsBadSyntax();
        TestThat.theInput("{@require <" + current + "}").throwsBadSyntax();
    }

    @Test
    @DisplayName("Test that requiring a future version is in the future is okay")
    void testFutureVersionOK() throws Exception {
        final var current = Processor.jamalVersion();
        final var futureF = (current.feature() + 1) + "." + current.interim() + "." + current.update();
        TestThat.theInput("{@require <" + futureF + "}").results("");
        TestThat.theInput("{@require <=" + futureF + "}").results("");
        TestThat.theInput("{@require >" + futureF + "}").throwsBadSyntax();
        TestThat.theInput("{@require >=" + futureF + "}").throwsBadSyntax();
        TestThat.theInput("{@require =" + futureF + "}").throwsBadSyntax();
        final var futureI = current.feature() + "." + (current.interim() + 1) + "." + current.update();
        TestThat.theInput("{@require <" + futureI + "}").results("");
        TestThat.theInput("{@require <=" + futureI + "}").results("");
        TestThat.theInput("{@require >" + futureI + "}").throwsBadSyntax();
        TestThat.theInput("{@require >=" + futureI + "}").throwsBadSyntax();
        TestThat.theInput("{@require =" + futureI + "}").throwsBadSyntax();
        final var futureU = current.feature() + "." + current.interim() + "." + (current.update() + 1);
        TestThat.theInput("{@require <" + futureU + "}").results("");
        TestThat.theInput("{@require <=" + futureU + "}").results("");
        TestThat.theInput("{@require >" + futureU + "}").throwsBadSyntax();
        TestThat.theInput("{@require >=" + futureU + "}").throwsBadSyntax();
        TestThat.theInput("{@require =" + futureU + "}").throwsBadSyntax();

    }
}

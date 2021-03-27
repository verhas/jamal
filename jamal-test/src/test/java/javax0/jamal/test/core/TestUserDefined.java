package javax0.jamal.test.core;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

public class TestUserDefined {

    @Test
    void testDefault() throws Exception {
        TestThat.theInput("{@define default=wupppps...}{something}").results("wupppps...");
    }

    @Test
    void testIncomplete() throws Exception {
        TestThat.theInput("{@define z wupppps...}").throwsBadSyntax();
    }

    @Test
    void testDefaultOptional() throws Exception {
        TestThat.theInput("{@define default=wupppps...}{?something}").results("wupppps...");
    }

    @Test
    void testDefine() throws Exception {
        TestThat.theInput("{@define a=wupppps...}{a}").results("wupppps...");
    }

    @Test
    void testReDefine() throws Exception {
        TestThat.theInput("{@define a=y}{@define a=x}{a}").results("x");
    }

    @Test
    void testReDefineOptional() throws Exception {
        TestThat.theInput("{@define a=y}{@define ? a=x}{a}").results("y");
    }

    @Test
    void testReDefineError() throws Exception {
        TestThat.theInput("{@define a=y}{@define ! a=x}{a}").throwsBadSyntax();
    }

    @Test
    void testGlobal1() throws Exception {
        TestThat.theInput("{#ident {@define :a=y}}{@define ? a=x}{a}").results("y");
    }

    @Test
    void testGlobal2() throws Exception {
        TestThat.theInput("{#ident {@define a:()=y}}{a:}").results("y");
    }

    @Test
    void testPure() throws Exception {
        TestThat.theInput("{@define a:=y}{a}").results("y");
    }

    @Test
    void testPureWithArguments() throws Exception {
        TestThat.theInput("{@define a(z):=yz}{a Z}").results("yZ");
    }

    @Test
    void testPureAndUse() throws Exception {
        TestThat.theInput("{@define a(z):={yz}}{@define yZ=k}{@sep [ ]}[a Z]").results("{yZ}");
    }
    @Test
    void testInpureAndUse() throws Exception {
        TestThat.theInput("{@define a(z)={yz}}{@define yZ=k}{@sep [ ]}[a Z]").results("k");
    }

}

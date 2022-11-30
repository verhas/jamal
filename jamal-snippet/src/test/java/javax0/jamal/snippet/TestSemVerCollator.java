package javax0.jamal.snippet;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestSemVerCollator {

    @Test
    void testSimpleVersionCompare() throws Exception {
        final var sut = new SemVerCollator();
        Assertions.assertTrue(0 > sut.compare("1.0.0", "1.0.1"));
        Assertions.assertTrue(0 > sut.compare("1.0.A", "1.0.B"));
        Assertions.assertTrue(0 < sut.compare("1.0.A", "1.0.0"));
        Assertions.assertTrue(0 > sut.compare("1.0.0", "2.0.0"));
        Assertions.assertTrue(0 > sut.compare("2.0.0", "2.1.0"));
        Assertions.assertTrue(0 > sut.compare("2.1.0", "2.1.1"));
        Assertions.assertTrue(0 > sut.compare("2.1.0", "2.1.1"));
    }

    @Test
    void negativeVersionCompare() throws Exception {
        final var sut = new SemVerCollator();
        Assertions.assertTrue(0 < sut.compare("1.0.1", "1.0.0"));
        Assertions.assertTrue(0 < sut.compare("1.0.C", "1.0.A"));
        Assertions.assertTrue(0 > sut.compare("1.0.9", "1.0.A"));
        Assertions.assertTrue(0 < sut.compare("2.0.0", "1.0.0"));
        Assertions.assertTrue(0 < sut.compare("2.1.0", "2.0.0"));
        Assertions.assertTrue(0 < sut.compare("2.1.1", "2.1.0"));
        Assertions.assertTrue(0 < sut.compare("2.1.1", "2.1.0"));
    }

    @Test
    void equalVersion() throws Exception {
        final var sut = new SemVerCollator();
        Assertions.assertEquals(0, sut.compare("1.0.1", "1.0.1"));
        Assertions.assertEquals(0, sut.compare("1.0.B", "1.0.B"));
        Assertions.assertEquals(0, sut.compare("1.0.0", "1.0.0"));
        Assertions.assertEquals(0, sut.compare("2.0.0", "2.0.0"));
        Assertions.assertEquals(0, sut.compare("2.1.0", "2.1.0"));
        Assertions.assertEquals(0, sut.compare("2.1.1", "2.1.1"));
        Assertions.assertEquals(0, sut.compare("2.1.1", "2.1.1"));
    }

    @Test
    void buildDoesNotMatter() throws Exception {
        final var sut = new SemVerCollator();
        Assertions.assertEquals(0, sut.compare("1.0.1+123", "1.0.1"));
        Assertions.assertEquals(0, sut.compare("1.0.B+123", "1.0.B+124"));
    }

    @Test
    void preReleaseVersionPrecedesVersion() throws Exception {
        final var sut = new SemVerCollator();
        Assertions.assertTrue(0 > sut.compare("1.0.0-alpha", "1.0.0"));
        Assertions.assertTrue(0 > sut.compare("1.0.A", "1.0.B-SNAPSHOT"));
        Assertions.assertTrue(0 > sut.compare("1.0.A-ANYTHING", "1.0.A"));
        Assertions.assertTrue(0 > sut.compare("1.0.0-ANYTHING", "1.0.0-BNYTHING"));
        Assertions.assertTrue(0 < sut.compare("1.0.0-BNYTHING", "1.0.0-ANYTHING"));
    }

    @Test
    void generalTests() throws Exception {
        final var sut = new SemVerCollator();
        // 1.0.0-alpha, 1.0.0-alpha.1, 1.0.0-0.3.7, 1.0.0-x.7.z.92, 1.0.0-x-y-z.–.
        Assertions.assertTrue(0 > sut.compare("1.0.0-alpha", "1.0.0-alpha.1"));
        Assertions.assertTrue(0 < sut.compare("1.0.0-alpha.1", "1.0.0-0.3.7"));
        Assertions.assertTrue(0 < sut.compare("1.0.0-alpha.1", "1.0.0-0.3.7+663"));
        Assertions.assertTrue(0 < sut.compare("1.0.0-alpha.beta","1.0.0-alpha.1"));
    }


    @Test
    void doSomeSorting() {
        final var sut = new SemVerCollator();
        final var versions = new ArrayList<>(List.of("1.0.0-alpha", "1.0.0-alpha.beta", "1.0.0-beta", "1.0.0-beta.2", "1.0.0-alpha.1", "1.0.0-beta.11", "1.0.0-rc.1", "1.0.0"));
        versions.sort(sut);
        Assertions.assertEquals(List.of("1.0.0-alpha", "1.0.0-alpha.1", "1.0.0-alpha.beta", "1.0.0-beta", "1.0.0-beta.2", "1.0.0-beta.11", "1.0.0-rc.1", "1.0.0"), versions);
    }


    private static int compareBA(final String a, final String b){
        return Arrays.compare(new SemVerCollator.SemVerKey(a).toByteArray(), new SemVerCollator.SemVerKey(b).toByteArray());
    }

    @Test
    void compareTheByteArrays() throws Exception {
        final var sut = new SemVerCollator();
        Assertions.assertTrue(0 > compareBA("1.0.0","1.0.1"));
        Assertions.assertTrue(0 > compareBA("1.0.A", "1.0.B"));
        Assertions.assertTrue(0 < compareBA("1.0.A", "1.0.0"));
        Assertions.assertTrue(0 > compareBA("1.0.0", "2.0.0"));
        Assertions.assertTrue(0 > compareBA("2.0.0", "2.1.0"));
        Assertions.assertTrue(0 > compareBA("2.1.0", "2.1.1"));
        Assertions.assertTrue(0 > compareBA("2.1.0", "2.1.1"));
        Assertions.assertTrue(0 > compareBA("1.0.0-alpha", "1.0.0-alpha.1"));
        Assertions.assertTrue(0 < compareBA("1.0.0-alpha.1", "1.0.0-0.3.7"));
        Assertions.assertTrue(0 < compareBA("1.0.0-alpha.1", "1.0.0-0.3.7+663"));
        Assertions.assertTrue(0 < compareBA("1.0.0-alpha.beta","1.0.0-alpha.1"));
    }

}

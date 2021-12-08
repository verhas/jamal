package javax0.jamal.snippet;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class TestDate {
    @Test
    void testDate() throws Exception {
        TestThat.theInput("{@date yyyy}").results(new GregorianCalendar().get(Calendar.YEAR)+"");
    }
}

package javax0.jamal.snippet;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestSortLines {

	@Test
	@DisplayName("sortLines - simple sorting")
	void testSortLines() throws Exception {
		TestThat.theInput(""
				+ "{@sortlines b\n"
				+ "a\n"
				+ "d\n"
				+ "c}"
		).results("a\n"
				+ "b\n"
				+ "c\n"
				+ "d");
	}

	@Test
	@DisplayName("sortLines - preserves newline at the end")
	void testSortLinesPreservesNewline() throws Exception {
		TestThat.theInput(""
				+ "{@sortlines b\n"
				+ "a\n"
				+ "d\n"
				+ "c\n"
				+ "}"
		).results("a\n"
				+ "b\n"
				+ "c\n"
				+ "d\n");
	}

}

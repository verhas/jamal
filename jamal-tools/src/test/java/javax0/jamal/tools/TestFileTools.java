package javax0.jamal.tools;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static javax0.jamal.tools.FileTools.absolute;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestFileTools {

    @Test
    @DisplayName("returns the file name when it is absolute starting with ~")
    public void testAbsoluteFileNameStartingWithTilde(){
        var result = absolute("/refer ence/notUsed","~absolute");
        assertEquals("~absolute",result);
    }

    @Test
    @DisplayName("returns the file name when it is absolute starting with /")
    public void testAbsoluteFileNameStartingWithSlash(){
        var result = absolute("/refer ence/notUsed","/absolute");
        assertEquals("/absolute",result);
    }

    @Test
    @DisplayName("returns the file name when it is absolute starting with backslash ")
    public void testAbsoluteFileNameStartingWithBackSlash(){
        var result = absolute("/refer ence/notUsed","\\absolute");
        assertEquals("\\absolute",result);
    }

    @Test
    @DisplayName("returns the file name when it is absolute starting with c: ")
    public void testAbsoluteFileNameStartingWithDriveLetter(){
        var result = absolute("/refer ence/notUsed","c:absolute");
        assertEquals("c:absolute",result);
    }
    @Test
    @DisplayName("returns the calculated file name when it is simple concatenation")
    public void testSimpleRelativeFileName(){
        var result = absolute("a/","b");
        assertEquals("a/b",result);
    }

    @Test
    @DisplayName("returns the calculated file name when relative file name starts with ./")
    public void testSimpleRelativeFileNameWithDotSlash(){
        var result = absolute("a/","./b");
        assertEquals("a/b",result);
    }

    @Test
    @DisplayName("returns the calculated file name when it is simple concatenation with file name in reference")
    public void testSimpleRelativeFileNameWithFileNameReference(){
        var result = absolute("a/z","b");
        assertEquals("a/b",result);
    }


    @Test
    @DisplayName("returns the calculated file name when it is simple concatenation with file name only in reference")
    public void testSimpleRelativeFileNameWithFileNameOnlyReference(){
        var result = absolute("a","b");
        assertEquals("b",result);
    }

    @Test
    @DisplayName("returns the calculated file name in complex case, reference is directory")
    public void testComplexRelativeFileNameWithReference(){
        var result = absolute("a/b/c/d/","e/f/g");
        assertEquals("a/b/c/d/e/f/g",result);
    }

    @Test
    @DisplayName("returns the calculated file name in complex case, reference is file")
    public void testComplexRelativeFileNameWithFileNameReference(){
        var result = absolute("a/b/c/d/z","e/f/g");
        assertEquals("a/b/c/d/e/f/g",result);
    }

    @Test
    @DisplayName("one .. in file name")
    public void testComplexRelativeFileNameWithFileNameReferenceWalkUpInFileName(){
        var result = absolute("a/b/c/d/z","../e/f/g");
        assertEquals("a/b/c/e/f/g",result);
    }

    @Test
    @DisplayName("multiple .. in file name")
    public void testComplexRelativeFileNameWithFileNameReferenceWalkUpTwiceInFileName(){
        var result = absolute("a/b/c/d/z","../../e/f/g");
        assertEquals("a/b/e/f/g",result);
    }

    @Test
    @DisplayName("/./ in file name")
    public void testComplexRelativeFileNameWithFileNameReferenceWalkUpTwiceInFileNameMultipleDots(){
        var result = absolute("a/b/c/d/z","../../e/./f/g");
        assertEquals("a/b/e/f/g",result);
    }

    @Test
    @DisplayName(".. off the reference structure")
    public void testComplexRelativeFileNameWithTooManyDotDots(){
        var result = absolute("a/b/c/d/z","../../../../../e/f/g");
        assertEquals("../e/f/g",result);
    }
}

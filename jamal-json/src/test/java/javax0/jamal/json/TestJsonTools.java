package javax0.jamal.json;

import javax0.jamal.api.MacroRegister;
import javax0.jamal.api.Processor;
import org.json.JSONObject;
import org.json.JSONPointer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestJsonTools {

    public static final String MOCK_MACRO_NAME = "json:object";

    private static Processor mockProcessorFor(final String id, JsonMacroObject jsonMacroObject) {
        final var processor = mock(Processor.class);
        final var register = mock(MacroRegister.class);
        when(processor.getRegister()).thenReturn(register);
        when(register.getUserDefined(id)).thenReturn(Optional.ofNullable(jsonMacroObject));
        return processor;
    }

    @DisplayName("Test get macro, path and key for single macro id")
    @Test
    void testGetMacroPathKeyForSingleMacroId() throws Exception {
        final var jsonMacroObject = new JsonMacroObject(MOCK_MACRO_NAME, "value");
        final var processor = mockProcessorFor(MOCK_MACRO_NAME, jsonMacroObject);
        final var tools = new JsonTools(processor);
        final var mpk = tools.getMacroPathKey(MOCK_MACRO_NAME);
        Assertions.assertNotNull(mpk);
        Assertions.assertNull(mpk.json);
        Assertions.assertNull(mpk.key);
        Assertions.assertNull(mpk.path);
        Assertions.assertEquals(MOCK_MACRO_NAME, mpk.macroId);
    }

    @DisplayName("Test get macro, path and key for single macro id and key")
    @Test
    void testGetMacroPathKeyForMacroIdAndKey() throws Exception {
        final JSONObject testJson = new JSONObject("{a:1,b:2}");
        final var jsonMacroObject = new JsonMacroObject(MOCK_MACRO_NAME, testJson);
        final var processor = mockProcessorFor(MOCK_MACRO_NAME, jsonMacroObject);
        final var tools = new JsonTools(processor);
        final var mpk = tools.getMacroPathKey(MOCK_MACRO_NAME + "/c");
        Assertions.assertNotNull(mpk);
        Assertions.assertSame(testJson,mpk.json);
        Assertions.assertEquals("c", mpk.key);
        Assertions.assertNull(mpk.path);
        Assertions.assertEquals(MOCK_MACRO_NAME, mpk.macroId);
    }

    @DisplayName("Test get macro, path and key for single macro id and '*'")
    @Test
    void testGetMacroPathKeyForMacroIdAndStar() throws Exception {
        final JSONObject testJson = new JSONObject("{a:1,b:2}");
        final var jsonMacroObject = new JsonMacroObject(MOCK_MACRO_NAME, testJson);
        final var processor = mockProcessorFor(MOCK_MACRO_NAME, jsonMacroObject);
        final var tools = new JsonTools(processor);
        final var mpk = tools.getMacroPathKey(MOCK_MACRO_NAME + "/*");
        Assertions.assertNotNull(mpk);
        Assertions.assertSame(testJson,mpk.json);
        Assertions.assertEquals("*", mpk.key);
        Assertions.assertNull(mpk.path);
        Assertions.assertEquals(MOCK_MACRO_NAME, mpk.macroId);
    }

    @DisplayName("Test get full path...")
    @Test
    void testGetMacroPathKeyFullPath() throws Exception {
        final JSONObject testJson = new JSONObject("{a:[],b:2}");
        final var jsonMacroObject = new JsonMacroObject(MOCK_MACRO_NAME, testJson);
        final var processor = mockProcessorFor(MOCK_MACRO_NAME, jsonMacroObject);
        final var tools = new JsonTools(processor);
        final var mpk = tools.getMacroPathKey(MOCK_MACRO_NAME + "/a/*");
        Assertions.assertNotNull(mpk);
        Assertions.assertSame(testJson,mpk.json);
        Assertions.assertEquals("*", mpk.key);
        Assertions.assertInstanceOf(JSONPointer.class,mpk.path);
        Assertions.assertEquals("/a",mpk.path.toString());
        Assertions.assertEquals(MOCK_MACRO_NAME, mpk.macroId);
    }

}

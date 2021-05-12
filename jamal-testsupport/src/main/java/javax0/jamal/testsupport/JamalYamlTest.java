package javax0.jamal.testsupport;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Position;
import javax0.jamal.engine.Processor;
import javax0.jamal.tools.Input;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class JamalYamlTest {
    private static final List<String> keywords = List.of("Input", "Output", "Throws", "Details");
    public static final String __OFF__ = "__OFF__";
    public static final String __ON__ = "__ON__";
    public static final String __SKIP__ = "__SKIP__";

    public static JamalTests factory(String... testFiles) {
        final var klass = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass();

        if (testFiles == null || testFiles.length == 0) {
            return new JamalYamlTest().factoryOne(klass, klass.getSimpleName());
        } else {
            final var dynamicContainers = new JamalTests<DynamicContainer>();
            boolean on = true;
            boolean oneOff = false;
            for (final var s : testFiles) {
                if (s == null) {
                    break;
                }
                switch (s) {
                    case __OFF__:
                        on = false;
                        break;
                    case __ON__:
                        on = true;
                        break;
                    case __SKIP__:
                        oneOff = true;
                        break;
                    default:
                        if (on && !oneOff) {
                            dynamicContainers.add(DynamicContainer.dynamicContainer(s, new JamalYamlTest().factoryOne(klass, s)));
                        }else{
                            dynamicContainers.add(DynamicContainer.dynamicContainer(s,List.of(DynamicTest.dynamicTest(s, () -> Assumptions.assumeTrue(false,"Test was skipped")))));
                        }
                        oneOff = false;
                        break;
                }
            }
            return dynamicContainers;
        }
    }


    final Yaml yaml = new Yaml();

    public JamalTests factoryOne(Class<?> klass, String testFile) {
        final JamalTests<DynamicNode> dynamicTests = new JamalTests<>();
        final InputStream is = getInputStream(klass, testFile, dynamicTests);
        if (dynamicTests.size() > 0) {
            return dynamicTests;
        }
        final StringBuffer sb = readFileContent(testFile, is, dynamicTests);
        if (dynamicTests.size() > 0) {
            return dynamicTests;
        }
        final String processed = process(testFile, sb, dynamicTests);
        if (dynamicTests.size() > 0) {
            return dynamicTests;
        }
        final Map<String, Map<String, Object>> tests = loadYaml(testFile, processed, dynamicTests);
        if (dynamicTests.size() > 0) {
            return dynamicTests;
        }
        return addTests(tests, dynamicTests);
    }

    private JamalTests<DynamicNode> addTests(Map<String, Map<String, Object>> tests, JamalTests<DynamicNode> dynamicNodes) {
        for (final var e : tests.entrySet()) {
            if ("end".equals(e.getKey())) {
                break;
            }
            if (e.getValue() != null && keywords.stream().noneMatch(k -> e.getValue().containsKey(k))) {
                dynamicNodes.add(DynamicContainer.dynamicContainer(e.getKey(), new JamalYamlTest().addTests((Map) e.getValue(), new JamalTests<>())));
            } else {
                if (e.getValue() == null) {
                    dynamicNodes.add(DynamicTest.dynamicTest(e.getKey(), () -> Assertions.fail(e.getKey() + " has to describe a Map in Yaml format")));
                } else {
                    dynamicNodes.add(DynamicTest.dynamicTest(e.getKey(), () -> exec(e.getKey(), e.getValue())));
                }
            }
        }
        return dynamicNodes;
    }

    private Map<String, Map<String, Object>> loadYaml(String testFile, String processed, JamalTests<DynamicNode> dynamicTests) {
        final Object tests;
        try {
            tests = yaml.load(processed);
            if (tests instanceof Map) {
                return (Map<String, Map<String, Object>>) tests;
            } else {
                dynamicTests.add(DynamicTest.dynamicTest(testFile, () -> Assertions.fail(testFile + ".jyt has to describe a Map in Yaml format")));
            }
        } catch (Exception e) {
            dynamicTests.add(DynamicTest.dynamicTest(testFile, () -> Assertions.fail(testFile + ".jyt cannot be processed by Yaml.", e)));
        }
        return null;
    }

    private String process(String testFile, StringBuffer sb, JamalTests<DynamicNode> dynamicTests) {
        final var processor = new Processor("{%", "%}");
        final String processed;
        try {
            processed = processor.process(Input.makeInput(sb.toString(), new Position(testFile + ".jyt")));
            return processed;
        } catch (BadSyntax e) {
            dynamicTests.add(DynamicTest.dynamicTest(testFile, () -> Assertions.fail(testFile + ".jyt cannot be processed by Jamal.", e)));
        }
        return null;
    }

    private StringBuffer readFileContent(String testFile, InputStream is, JamalTests<DynamicNode> dynamicTests) {
        final var sb = new StringBuffer();
        try (InputStreamReader isReader = new InputStreamReader(is, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(isReader)) {
            String str;
            while ((str = reader.readLine()) != null) {
                sb.append(str).append("\n");
            }
        } catch (IOException e) {
            dynamicTests.add(DynamicTest.dynamicTest(testFile, () -> Assertions.fail(testFile + ".jyt cannot be read.", e)));
        }
        return sb;
    }

    private InputStream getInputStream(Class<?> klass, String testFile, JamalTests<DynamicNode> dynamicTests) {
        final var is = klass.getResourceAsStream(testFile + ".jyt");
        if (is == null) {
            dynamicTests.add(DynamicTest.dynamicTest(testFile, () -> Assertions.fail(testFile + ".jyt is not found")));
        }
        return is;
    }

    public static void exec(String displayName, Map<String, Object> testStructure) throws BadSyntax, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        if (testStructure == null) {
            throw new IllegalAccessException("Test '" + displayName + "' has null body");
        }
        final var illegalKeys = new ArrayList<String>();
        for (final var key : testStructure.keySet()) {
            if (!keywords.contains(key)) {
                illegalKeys.add(key);
            }
        }
        if (illegalKeys.size() > 0) {
            throw new IllegalArgumentException("There are illegal keys in the test '" + displayName + "': " + String.join(", ", illegalKeys));
        }
        final var input = Objects.requireNonNull(testStructure.get("Input"), "Input must present for the tests '" + displayName + "'");
        final var output = testStructure.get("Output");
        final var aThrows = testStructure.get("Throws");
        if ((output == null) == (aThrows == null)) {
            throw new IllegalArgumentException("The test '" + displayName + "' must have either 'Output' or 'Throws' and never both.");
        }
        if (output == null) {
            TestThat.theInput((String) input).throwsBadSyntax((String) aThrows);
        } else {
            TestThat.theInput((String) input).results((String) output);
        }
    }
}

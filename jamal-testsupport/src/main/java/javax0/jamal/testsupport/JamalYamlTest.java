package javax0.jamal.testsupport;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Position;
import javax0.jamal.engine.Processor;
import javax0.jamal.tools.Input;
import org.junit.jupiter.api.Assertions;
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
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

public class JamalYamlTest {
    public static Collection<? extends DynamicNode> factory(String... testFiles) {
        final var klass = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass();

        if (testFiles == null || testFiles.length == 0) {
            return new JamalYamlTest().factoryOne(klass, klass.getSimpleName());
        } else {
            final var dynamicContainers = new ArrayList<DynamicContainer>();
            for (final var s : testFiles) {
                dynamicContainers.add(DynamicContainer.dynamicContainer(s, new JamalYamlTest().factoryOne(klass, s)));
            }
            return dynamicContainers;
        }
    }


    final ArrayList<DynamicTest> dynamicTests = new ArrayList<>();
    final Yaml yaml = new Yaml();

    public Collection<DynamicTest> factoryOne(Class<?> klass, String testFile) {
        final InputStream is = getInputStream(klass, testFile);
        if (dynamicTests.size() > 0) {
            return dynamicTests;
        }
        final StringBuffer sb = readFileContent(testFile, is);
        if (dynamicTests.size() > 0) {
            return dynamicTests;
        }
        final String processed = process(testFile, sb);
        if (dynamicTests.size() > 0) {
            return dynamicTests;
        }
        final Map<String, Map<String, String>> tests = loadYaml(testFile, processed);
        if (dynamicTests.size() > 0) {
            return dynamicTests;
        }
        for (final var e : tests.entrySet()) {
            if ("end".equals(e.getKey())) {
                break;
            }
            dynamicTests.add(DynamicTest.dynamicTest(e.getKey(), () -> exec(e.getKey(), e.getValue())));
        }
        return dynamicTests;
    }

    private Map<String, Map<String, String>> loadYaml(String testFile, String processed) {
        final Object tests;
        try {
            tests = yaml.load(processed);
            if (tests instanceof Map) {
                return (Map<String, Map<String, String>>) tests;
            } else {
                dynamicTests.add(DynamicTest.dynamicTest(testFile, () -> Assertions.fail(testFile + ".jyt has to describe a Map in Yaml format")));
            }
        } catch (Exception e) {
            dynamicTests.add(DynamicTest.dynamicTest(testFile, () -> Assertions.fail(testFile + ".jyt cannot be processed by Yaml.", e)));
        }
        return null;
    }

    private String process(String testFile, StringBuffer sb) {
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

    private StringBuffer readFileContent(String testFile, InputStream is) {
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

    private InputStream getInputStream(Class<?> klass, String testFile) {
        final var is = klass.getResourceAsStream(testFile + ".jyt");
        if (is == null) {
            dynamicTests.add(DynamicTest.dynamicTest(testFile, () -> Assertions.fail(testFile + ".jyt is not found")));
        }
        return is;
    }

    public static void exec(String displayName, Map<String, String> testStructure) throws BadSyntax, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        if (testStructure == null) {
            throw new IllegalAccessException("Test '" + displayName + "' has null body");
        }
        final var input = Objects.requireNonNull(testStructure.get("Input"), "Input must present for all tests.");
        final var output = testStructure.get("Output");
        final var aThrows = testStructure.get("Throws");
        if ((output == null) == (aThrows == null)) {
            throw new IllegalArgumentException("The test '" + displayName + "' must have either 'Output' or 'Throw' and never both.");
        }
        if (output == null) {
            TestThat.theInput(input).throwsBadSyntax(aThrows);
        } else {
            TestThat.theInput(input).results(output);
        }
    }
}

package javax0.jamal.openai;

import javax0.jamal.api.EnvironmentVariables;

import java.util.Optional;

public class ConfigurationReader {

    public static Optional<String> getApiKey() {
        return EnvironmentVariables.getenv("OPENAI_API_KEY");
    }

    public static String getUrl() {
        return EnvironmentVariables.getenv("OPENAI_URL").orElse("https://api.openai.com/v1/");
    }

    public static Optional<String> getOrganization() {
        return EnvironmentVariables.getenv("OPENAI_ORGANIZATION");
    }
}

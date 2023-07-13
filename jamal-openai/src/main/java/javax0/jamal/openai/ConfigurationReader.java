package javax0.jamal.openai;

import javax0.jamal.api.EnvironmentVariables;

import java.util.Optional;

public class ConfigurationReader {

    public static Optional<String> getApiKey() {
    // snipline OPENAI_API_KEY filter="(.*)"
        return EnvironmentVariables.getenv("OPENAI_API_KEY");
    }

    public static Optional<String> getOrganization() {
    // snipline OPENAI_ORGANIZATION filter="(.*)"
        return EnvironmentVariables.getenv("OPENAI_ORGANIZATION");
    }
}

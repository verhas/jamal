package javax0.jamal.openai;

import javax0.jamal.api.EnvironmentVariables;

import java.util.Optional;

public class ConfigurationReader {

    public static Optional<String> getApiKey() {
        return EnvironmentVariables.getenv("OPENAI_API_KEY");
    }

    public static Optional<String> getOrganization() {
        return EnvironmentVariables.getenv("OPENAI_ORGANIZATION");
    }
}

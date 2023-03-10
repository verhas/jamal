package javax0.jamal.openai;

import org.json.JSONObject;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;
import org.yaml.snakeyaml.resolver.Resolver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Query {

    private String url = ConfigurationReader.getUrl();

    public Object getModel(final String name) throws IOException {
        final var con = (HttpURLConnection) new URL(url + "models/" + name).openConnection();
        return get(con);
    }

    public Object getModels() throws IOException {
        final var con = (HttpURLConnection) new URL(url + "models").openConnection();
        return get(con);
    }

    public Object getCompletion(Map<String, Object> parameters) throws IOException {
        final var con = (HttpURLConnection) new URL(url + "completions").openConnection();
        return post(con, parameters);
    }

    private Object post(final HttpURLConnection con, Map<String, Object> params) throws IOException {
        try {
            con.setRequestMethod("POST");
            con.setRequestProperty("Authorization", "Bearer " + ConfigurationReader.getApiKey().orElseThrow());
            final var org = ConfigurationReader.getOrganization();
            org.ifPresent(s -> con.setRequestProperty("OpenAI-Organization", s));
            con.setInstanceFollowRedirects(true);
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);
            JSONObject json = new JSONObject(params);
            con.getOutputStream().write(json.toString().getBytes(StandardCharsets.UTF_8));
            final int status = con.getResponseCode();
            final var reader = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
            final var retval = reader.lines().collect(Collectors.joining("\n"));
            if (status != 200) {
                throw new IOException("GET url '" + url + "' returned " + status);
            }
            final var result = new JSONObject(retval);
            return result;
        } finally {
            con.disconnect();
        }
    }

    private static final YamlNoImplicitTagResolver noImplicitTagResolver = new YamlNoImplicitTagResolver();

    private Object get(final HttpURLConnection con) throws IOException {
        try {
            con.setRequestMethod("GET");
            con.setRequestProperty("Authorization", "Bearer " + ConfigurationReader.getApiKey().orElseThrow());
            final var org = ConfigurationReader.getOrganization();
            org.ifPresent(s -> con.setRequestProperty("OpenAI-Organization", s));
            con.setInstanceFollowRedirects(true);
            final int status = con.getResponseCode();
            if (status != 200) {
                throw new IOException("GET url '" + url + "' returned " + status);
            }
            final var reader = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
            final var retval = reader.lines().collect(Collectors.joining("\n"));
            final var yaml = new Yaml();
            final var result = yaml.load(retval);
            return result;
        } finally {
            con.disconnect();
        }
    }


    public static class YamlNoImplicitTagResolver extends Resolver {
        @Override
        protected void addImplicitResolvers() {
        }
        public void addImplicitResolver(Tag tag, Pattern regexp, String first) {

        }

        @Override
        public Tag resolve(NodeId kind, String value, boolean implicit) {
            return super.resolve(kind,value,implicit);
        }
    }
}


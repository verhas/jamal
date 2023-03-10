package javax0.jamal.openai;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TestGetModels {

    @Test
    void getTheModels() throws Exception {
        final var query = new Query();
        final var models = (Map<String,Object>)query.getModels();
        System.out.println(models);
        for( final var model : ((ArrayList<Map>)models.get("data"))){
            System.out.println(model.get("id"));
        }
    }

    @Test
    void getBabbageModel()throws Exception{
        final var query = new Query();
        final var model = (Map<String,Object>)query.getModel("babbage");
        System.out.println(model);
    }

    @Test
    void testCompletions() throws Exception {
        final var query = new Query();
        final Map<String,Object> params = new HashMap<>(Map.of(
                "model", "text-davinci-003",
                "prompt", "Say this is a test",
                "max_tokens", 7,
                "temperature", 0));
        final var result = query.getCompletion(params);
        System.out.println(result);
    }
}

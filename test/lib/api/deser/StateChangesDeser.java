package lib.api.deser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import lib.api.StateChanges;

import java.io.IOException;

public class StateChangesDeser extends StdDeserializer<StateChanges> {

    @Override
    public StateChanges deserialize(JsonParser jp, DeserializationContext ctx) throws IOException, JsonProcessingException {
        StateChanges sc = new StateChanges();
        JsonNode node = jp.getCodec().readTree(jp);
        node.get("stateChanges").get("data")
        return null;
    }
}

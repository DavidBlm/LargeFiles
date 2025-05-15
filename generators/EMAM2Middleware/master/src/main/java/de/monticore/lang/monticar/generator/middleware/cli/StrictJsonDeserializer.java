/* (c) https://github.com/MontiCore/monticore */
package de.monticore.lang.monticar.generator.middleware.cli;

import com.google.gson.*;
import de.se_rwth.commons.logging.Log;

import java.lang.reflect.Type;
import java.util.List;

public class StrictJsonDeserializer<T> implements JsonDeserializer<T> {

    private final List<String> fieldNames;
    private final Gson delegate;

    public StrictJsonDeserializer(List<String> fieldNames, Gson delegate) {
        this.fieldNames = fieldNames;
        this.delegate = delegate;
    }

    @Override
    public T deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        if (!jsonElement.isJsonObject()) {
            Log.error("Expecting json object!");
        } else {
            JsonObject jsonObj = jsonElement.getAsJsonObject();
            for (String s : jsonObj.keySet()) {
                if (!fieldNames.contains(s)) {
                    Log.error("0x9DB6E: " + s + " is not a valid field name! Valid options: " + fieldNames);
                }
            }
        }
        return delegate.fromJson(jsonElement, type);
    }
}

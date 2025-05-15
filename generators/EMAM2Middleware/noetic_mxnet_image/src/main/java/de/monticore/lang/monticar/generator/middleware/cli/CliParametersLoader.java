/* (c) https://github.com/MontiCore/monticore */
package de.monticore.lang.monticar.generator.middleware.cli;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import de.monticore.lang.monticar.clustering.cli.algorithms.dynamic.*;
import de.se_rwth.commons.logging.Log;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CliParametersLoader {
    private CliParametersLoader() {
    }


    public static CliParameters loadCliParameters(String filePath) throws FileNotFoundException {
        JsonReader jsonReader = new JsonReader(new FileReader(filePath));
        JsonDeserializer<DynamicParameter> deserializer = (jsonElement, type, jsonDeserializationContext) -> {
            if(jsonElement.isJsonObject()){
                JsonObject jsonObj = (JsonObject) jsonElement;
                Double min = jsonObj.has("min") ? jsonObj.get("min").getAsDouble() : null;
                Double max = jsonObj.has("max") ? jsonObj.get("max").getAsDouble() : null;
                Double step = jsonObj.has("step") ? jsonObj.get("step").getAsDouble() : null;
                Integer count = jsonObj.has("count") ? jsonObj.get("count").getAsInt() : null;
                return new GeneratorParamter(min,max,step,count);
            }else if(jsonElement.isJsonArray()) {
                JsonArray jArray = (JsonArray) jsonElement;
                List<Double> values = new ArrayList<>();
                for(int i = 0; i < jArray.size(); i++){
                    values.add(jArray.get(i).getAsDouble());
                }
                return new ListParameter(values);
            }else{
                return new ListParameter(jsonElement.getAsDouble());
            }
        };

        Gson delegateGson = new GsonBuilder()
                .registerTypeAdapter(DynamicAlgorithmCliParameters.class, new AlgorithmParametersInterfaceAdapter())
                .registerTypeAdapter(DynamicParameter.class, deserializer)
                .create();


        JsonDeserializer<CliParameters> desCliParameters = new StrictJsonDeserializer<>(Arrays.asList("emadlBackend","writeTagFile","clusteringParameters","modelsDir","outputDir","rootModel","generators"), delegateGson);
        JsonDeserializer<ClusteringParameters> desClustering = new StrictJsonDeserializer<>(Arrays.asList("numberOfClusters","flatten","flattenLevel","chooseBy","algorithmParameters", "metric"), delegateGson);

        JsonDeserializer<DynamicSpectralClusteringCliParameters> desSpectral = new StrictJsonDeserializer<>(Arrays.asList("numberOfClusters","l","sigma"), delegateGson);
        JsonDeserializer<DynamicMarkovCliParameters> desMarkov = new StrictJsonDeserializer<>(Arrays.asList("max_residual","gamma_exp","loop_gain","zero_max"), delegateGson);
        JsonDeserializer<DynamicDBScanCliParameters> desDBScan = new StrictJsonDeserializer<>(Arrays.asList("min_pts","radius"), delegateGson);
        JsonDeserializer<DynamicAffinityPropagationCliParameters> desAff = new StrictJsonDeserializer<>(Collections.emptyList(), delegateGson);

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(DynamicSpectralClusteringCliParameters.class, desSpectral)
                .registerTypeAdapter(DynamicMarkovCliParameters.class, desMarkov)
                .registerTypeAdapter(DynamicDBScanCliParameters.class, desDBScan)
                .registerTypeAdapter(DynamicAffinityPropagationCliParameters.class, desAff)
                .registerTypeAdapter(CliParameters.class, desCliParameters)
                .registerTypeAdapter(ClusteringParameters.class, desClustering)
                .create();

        return gson.fromJson(jsonReader, CliParameters.class);
    }

    static final class AlgorithmParametersInterfaceAdapter implements JsonDeserializer<DynamicAlgorithmCliParameters> {

        public DynamicAlgorithmCliParameters deserialize(JsonElement elem, Type interfaceType, JsonDeserializationContext context) throws JsonParseException {
            final Type actualType = typeForName(((JsonObject)elem).get("name"));
            return context.deserialize(elem, actualType);
        }

        private Type typeForName(final JsonElement typeElem){
            String algoName = typeElem.getAsString().toLowerCase();
            switch (algoName){
                case "spectralclustering": return DynamicSpectralClusteringCliParameters.class;
                case "dbscan": return DynamicDBScanCliParameters.class;
                case "affinitypropagation": return DynamicAffinityPropagationCliParameters.class;
                case "markov": return DynamicMarkovCliParameters.class;
                default:{
                    Log.warn("Loaded config of unknown clustering algorithm: " + algoName);
                    return DynamicUnknownAlgorithmCliParameters.class;
                }
            }
        }
    }

}

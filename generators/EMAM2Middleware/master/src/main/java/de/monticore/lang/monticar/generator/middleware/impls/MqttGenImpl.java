/* (c) https://github.com/MontiCore/monticore */
package de.monticore.lang.monticar.generator.middleware.impls;

import java.io.File;
import java.io.IOException;
import java.util.List;

import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAPortInstanceSymbol;
import de.monticore.lang.monticar.generator.mqtt.GeneratorMqtt;
import de.monticore.lang.tagging._symboltable.TaggingResolver;
import de.se_rwth.commons.logging.Log;

public class MqttGenImpl implements GeneratorImpl {
	
	private String generationTargetPath;
    private GeneratorMqtt generatorMqtt;

    public MqttGenImpl(){
    	generatorMqtt = new GeneratorMqtt();
    }
    
    public void setGeneratorMqttCpp(GeneratorMqtt generatorMqttCpp) {
        this.generatorMqtt = generatorMqttCpp;
    }

	@Override
	public List<File> generate(EMAComponentInstanceSymbol componentInstanceSymbol, TaggingResolver taggingResolver)
			throws IOException {
		generatorMqtt.setGenerationTargetPath(generationTargetPath);
		return generatorMqtt.generateMqttAdapter(componentInstanceSymbol);
	}

	@Override
	public void setGenerationTargetPath(String path) {
		this.generationTargetPath = path;
	}

	@Override
	public boolean willAccept(EMAComponentInstanceSymbol componentInstanceSymbol) {
		
		boolean result = componentInstanceSymbol.getPortInstanceList().stream().anyMatch(EMAPortInstanceSymbol::isMqttPort);
        if(!result){
            Log.warn("GeneratorMqttCpp: No MQTT Ports found! Ignoring component " + componentInstanceSymbol.getName());
        }

        return result;
	}

}

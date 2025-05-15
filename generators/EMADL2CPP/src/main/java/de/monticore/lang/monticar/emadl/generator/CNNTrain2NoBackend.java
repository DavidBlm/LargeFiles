/* (c) https://github.com/MontiCore/monticore */
package de.monticore.lang.monticar.emadl.generator;

import de.monticore.lang.monticar.cnnarch.generator.CNNTrainGenerator;
import de.monticore.lang.monticar.cnnarch.generator.training.TrainingComponentsContainer;
import de.monticore.lang.monticar.cnnarch.generator.training.TrainingConfiguration;
import de.monticore.lang.monticar.cnnarch.tensorflowgenerator.CNNArch2TensorflowTrainParamSupportChecker;
import de.monticore.lang.monticar.generator.FileContent;

import java.nio.file.Path;
import java.util.*;

public class CNNTrain2NoBackend extends CNNTrainGenerator {

    public CNNTrain2NoBackend() {
        super(new CNNArch2TensorflowTrainParamSupportChecker());
    }

    @Override
    public void generate(Path modelsDirPath, String rootModelName) {
        //Nothing to generate..
    }

    @Override
    public List<FileContent> generateStrings(TrainingConfiguration trainingConfiguration,
                                             TrainingComponentsContainer trainingComponentsContainer,
                                             Path outputPath) {
        FileContent temp = new FileContent("print ('No Backend Selected!')","CNNTrainer_" + getInstanceName() + ".py");
        List<FileContent>  ret = new ArrayList<>();
        ret.add(temp);
        return ret;
    }
}
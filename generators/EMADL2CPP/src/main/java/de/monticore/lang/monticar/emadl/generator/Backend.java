/* (c) https://github.com/MontiCore/monticore */
package de.monticore.lang.monticar.emadl.generator;

import de.monticore.lang.monticar.cnnarch.caffe2generator.CNNArch2Caffe2;
import de.monticore.lang.monticar.cnnarch.caffe2generator.CNNTrain2Caffe2;
import de.monticore.lang.monticar.cnnarch.generator.CNNArchGenerator;
import de.monticore.lang.monticar.cnnarch.generator.CNNTrainGenerator;
import de.monticore.lang.monticar.cnnarch.gluongenerator.CNNArch2Gluon;
import de.monticore.lang.monticar.cnnarch.gluongenerator.CNNTrain2Gluon;
import de.monticore.lang.monticar.cnnarch.mxnetgenerator.CNNArch2MxNet;
import de.monticore.lang.monticar.cnnarch.mxnetgenerator.CNNTrain2MxNet;
import de.monticore.lang.monticar.cnnarch.pytorchgenerator.CNNArch2PyTorch;
import de.monticore.lang.monticar.cnnarch.tensorflowgenerator.CNNArch2Tensorflow;
import de.monticore.lang.monticar.cnnarch.tensorflowgenerator.CNNTrain2Tensorflow;
import de.monticore.lang.monticar.emadl.generator.reinforcementlearning.RewardFunctionCppGenerator;

import java.util.Optional;

public enum Backend {
    NONE {
        @Override
        public CNNArchGenerator getCNNArchGenerator() {
            return new CNNArch2NoBackend();
        }

        @Override
        public CNNTrainGenerator getCNNTrainGenerator() {
            return new CNNTrain2NoBackend();
        }
    },
    MXNET {
        @Override
        public CNNArchGenerator getCNNArchGenerator() {
            return new CNNArch2MxNet();
        }

        @Override
        public CNNTrainGenerator getCNNTrainGenerator() {
            return new CNNTrain2MxNet();
        }
    },
    CAFFE2 {
        @Override
        public CNNArchGenerator getCNNArchGenerator() {
            return new CNNArch2Caffe2();
        }

        @Override
        public CNNTrainGenerator getCNNTrainGenerator() {
            return new CNNTrain2Caffe2();
        }
    },
    GLUON {
        @Override
        public CNNArchGenerator getCNNArchGenerator() {
            return new CNNArch2Gluon();
        }

        @Override
        public CNNTrainGenerator getCNNTrainGenerator() {
            return new CNNTrain2Gluon(new RewardFunctionCppGenerator());
        }
    },
    TENSORFLOW {
        @Override
        public CNNArchGenerator getCNNArchGenerator() {
            return new CNNArch2Tensorflow();
        }

        @Override
        public CNNTrainGenerator getCNNTrainGenerator() {
            return new CNNTrain2Tensorflow();
        }
    },
    PYTORCH {
        @Override
        public CNNArchGenerator getCNNArchGenerator() {
            return new CNNArch2PyTorch();
        }

        @Override
        public CNNTrainGenerator getCNNTrainGenerator() {
            return new CNNTrain2NoBackend();
        }
    };

    public abstract CNNArchGenerator getCNNArchGenerator();

    public abstract CNNTrainGenerator getCNNTrainGenerator();

    public static Optional<Backend> getBackendFromString(String backend) {
        switch (backend) {
            case "NONE":
                return Optional.of(NONE);

            case "MXNET":
                return Optional.of(MXNET);

            case "CAFFE2":
                return Optional.of(CAFFE2);
                
            case "GLUON":
                return Optional.of(GLUON);
                
            case "TENSORFLOW":
                return Optional.of(TENSORFLOW);

            case "PYTORCH":
                return Optional.of(PYTORCH);

            default:
                return Optional.empty();
        }
    }

    public static String getBackendString(Backend backend) {
        switch (backend) {
            case NONE:
                return "NONE";

            case CAFFE2:
                return "CAFFE2";
                
            case GLUON:
                return "GLUON";
                
            case TENSORFLOW:
                return "TENSORFLOW";

            case PYTORCH:
                return "PYTORCH";

            default:
                return "MXNET";
        }
    }
}

package de.monticore.mlpipelines.automl.trainalgorithms.efficientnet;

import de.monticore.lang.math._symboltable.expression.MathNumberExpressionSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.LayerSymbol;
import de.monticore.lang.monticar.types2._ast.ASTDimension;
import de.monticore.mlpipelines.automl.configuration.EfficientNetConfig;
import de.monticore.mlpipelines.automl.helper.ArchitectureHelper;
import de.monticore.mlpipelines.automl.helper.MathNumberExpressionWrapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EfficientNetEmadlBuilder {
    private final ArchitectureSymbol architecture;
    private final EfficientNetConfig config;
    private final String classesVariableName = "classes";
    private final String channelsVariableName = "channels";

    public EfficientNetEmadlBuilder(ArchitectureSymbol architecture, EfficientNetConfig config) {
        this.architecture = architecture;
        this.config = config;
    }

    public List<String> getEmadl() {
        List<String> lines = new ArrayList<>();
        lines.add(createHeaderEmadl());
        lines.addAll(createPortsEmadl());
        lines.addAll(createCNN());
        lines.add("}");
        return lines;
    }

    private String createHeaderEmadl() {
        String name = "EfficientNetB" + config.getPhi();
        String parameters = getHeaderParameters();
        return "component " + name + parameters + "{";
    }

    private List<String> createPortsEmadl() {
        ASTDimension dimensions = ArchitectureHelper.getImageDimension(architecture);
        int imageSize = getImageSize(dimensions);
        String line1 = "    ports in Z(0:255)^{1, " + imageSize + ", " + imageSize + "} image,";
        String line2 = "          out Q(0:1)^{classes} predictions;";
        List<String> lines = new ArrayList<>();
        lines.add(line1);
        lines.add(line2);
        return lines;
    }

    private Collection<String> createCNN() {
        //create string for cnn
        List<String> lines = new ArrayList<>();
        lines.add("");
        lines.add("    implementation CNN {");
        lines.add("");
        lines.addAll(createStemEmadl());
        lines.add("");
        lines.addAll(createConvEmadl());
        lines.add("");
        lines.addAll(createReductionConvEmadl());
        lines.add("");
        lines.addAll(createResidualBlockEmadl());
        lines.add("");
        lines.addAll(createReductionBlockEmadl());
        lines.add("");
        lines.addAll(createElementLayers());
        lines.add("    }");
        return lines;
    }

    private int getImageSize(ASTDimension dimensions) {
        MathNumberExpressionSymbol widthDim = (MathNumberExpressionSymbol) dimensions.getMatrixDim(2).getSymbol();
        MathNumberExpressionWrapper imageWidth = new MathNumberExpressionWrapper(widthDim);
        return imageWidth.getIntValue();
    }

    private String getHeaderParameters() {
        return "<classes=" + config.getNumberClasses() + ">";
    }

    private Collection<String> createStemEmadl() {
        List<String> lines = new ArrayList<>();
        lines.add("        def stem(" + channelsVariableName + "){");
        lines.add(
                "            Convolution(kernel=(3,3), channels=" + channelsVariableName + ", stride=(2, 2), padding=\"same\") ->");
        lines.add("            BatchNorm() ->");
        lines.add("            LeakyRelu()");
        lines.add("        }");
        return lines;
    }

    private Collection<String> createConvEmadl() {
        List<String> lines = new ArrayList<>();
        lines.add("        def conv(" + channelsVariableName + "){");
        lines.add(
                "            Convolution(kernel=(3,3), channels=" + channelsVariableName + ", stride=(1, 1), padding=\"same\") ->");
        lines.add("            BatchNorm() ->");
        lines.add("            LeakyRelu()");
        lines.add("        }");
        return lines;
    }

    private Collection<String> createReductionConvEmadl() {
        List<String> lines = new ArrayList<>();
        lines.add("        def reductionConv(" + channelsVariableName + "){");
        lines.add(
                "            Convolution(kernel=(3,3), channels=" + channelsVariableName + ", stride=(2, 2), padding=\"same\") ->");
        lines.add("            BatchNorm() ->");
        lines.add("            LeakyRelu()");
        lines.add("        }");
        return lines;
    }

    private Collection<String> createResidualBlockEmadl() {
        List<String> lines = new ArrayList<>();
        lines.add("        def residualBlock(" + channelsVariableName + "){");
        lines.add("            (");
        lines.add("                conv(channels=" + channelsVariableName + ") ->");
        lines.add("                conv(channels=" + channelsVariableName + ")");
        lines.add("            |");
        lines.add("                LeakyRelu()");
        lines.add("            ) ->");
        lines.add("            Add()");
        lines.add("        }");
        return lines;
    }

    private Collection<String> createReductionBlockEmadl() {
        List<String> lines = new ArrayList<>();
        lines.add("        def reductionBlock(" + channelsVariableName + "){");
        lines.add("            (");
        lines.add("                conv(channels=" + channelsVariableName + ") ->");
        lines.add("                conv(channels=" + channelsVariableName + ") ->");
        lines.add("                reductionConv(channels=" + channelsVariableName + ")");
        lines.add("            |");
        lines.add("                reductionConv(channels=" + channelsVariableName + ")");
        lines.add("            ) ->");
        lines.add("            Add()");
        lines.add("        }");
        return lines;
    }

    private Collection<String> createElementLayers() {
        List<String> lines = new ArrayList<>();
        lines.add("        image ->");
        List<LayerSymbol> layers = ArchitectureHelper.getLayerSymbols(architecture);
        for (LayerSymbol layer : layers) {
            lines.add("        " + createLayerEmadl(layer));
        }
        lines.add("        predictions;");
        return lines;
    }

    private String createLayerEmadl(LayerSymbol layer) {
        int channels = 0;
        switch (layer.getName()) {
            case "stem":
                channels = getLayerChannels(layer);
                return "stem(channels=" + channels + ") ->";
            case "residualBlock":
                int repetitions = getLayerDepth(layer);
                channels = getLayerChannels(layer);
                return "residualBlock(-> = " + repetitions + ", channels=" + channels + ") ->";
            case "reductionBlock":
                channels = getLayerChannels(layer);
                return "reductionBlock(channels=" + channels + ") ->";
            case "FullyConnected":
                return "FullyConnected(units=" + classesVariableName + ") ->";
            case "Softmax":
                return "Softmax() ->";
            case "Concatenate":
                return "Concatenate() ->";
            default:
                return "";
        }
    }

    private int getLayerChannels(LayerSymbol layer) {
        ArrayList expressions = ArchitectureHelper.getExpressions(layer);
        int channelsIndex = getChannelsIndex(layer.getName());
        MathNumberExpressionSymbol mathNumberExpression = (MathNumberExpressionSymbol) expressions.get(channelsIndex);
        MathNumberExpressionWrapper expression = new MathNumberExpressionWrapper(mathNumberExpression);
        return expression.getIntValue();
    }

    private int getLayerDepth(LayerSymbol layer) {
        ArrayList expressions = ArchitectureHelper.getExpressions(layer);
        int layerRepetitionsIndex = 3;
        MathNumberExpressionSymbol mathNumberExpression = (MathNumberExpressionSymbol) expressions.get(
                layerRepetitionsIndex);
        MathNumberExpressionWrapper expression = new MathNumberExpressionWrapper(mathNumberExpression);
        return expression.getIntValue();
    }

    private static int getChannelsIndex(String architectureElementName) {
        switch (architectureElementName) {
            case "residualBlock":
                return 5;
            case "reductionBlock":
            case "stem":
                return 1;
        }
        throw new IllegalArgumentException("Block type not supported");
    }
}

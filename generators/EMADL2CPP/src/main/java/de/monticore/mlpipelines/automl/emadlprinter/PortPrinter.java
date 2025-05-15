package de.monticore.mlpipelines.automl.emadlprinter;

import de.monticore.expressionsbasis._ast.ASTExpression;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.cncModel.EMAPortArraySymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.lang.monticar.common2._ast.ASTCommonMatrixType;
import de.monticore.lang.monticar.ts.references.MCASTTypeSymbolReference;
import de.monticore.lang.monticar.types2._ast.ASTDimension;
import de.monticore.lang.monticar.types2._ast.ASTElementType;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.symboltable.Symbol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class PortPrinter {
    protected final IndentPrinter printer;

    public PortPrinter(IndentPrinter printer) {
        this.printer = printer;
    }

    public void printPorts(ArchitectureSymbol arch) {
        Map<String, Collection<Symbol>> enclosedSymbols = arch.getSpannedScope()
                .getEnclosingScope()
                .get()
                .getLocalSymbols();

        if (enclosedSymbols.size() > 1)
            printer.print("ports ");

        Set<String> portNames = enclosedSymbols.keySet();
        for (int i = 0; i < portNames.size(); i++) {
            String portName = (String) portNames.toArray()[i];
            if (portName.equals(""))
                continue;
            if (!portName.equals("image") && !portName.equals("predictions"))
                continue;
            printPort(enclosedSymbols, portName);
            if (i < portNames.size() - 2)
                printer.print(", ");
        }
        printer.println(";");
    }

    private void printPort(Map<String, Collection<Symbol>> enclosedSymbols, String portName) {
        ArrayList symbolList = (ArrayList) enclosedSymbols.get(portName);
        if (symbolList.size() > 1) {
            EMAPortArraySymbol port = (EMAPortArraySymbol) symbolList.get(1);
            String portIncome = port.isIncoming() ? "in " : "out ";
            printer.print(portIncome);
            printPortType(port);
            printPortDimension(port);
        }
        this.printer.print(" " + portName);
    }


    private void printPortType(EMAPortArraySymbol port) {
        MCASTTypeSymbolReference typeReference = (MCASTTypeSymbolReference) port.getTypeReference();
        ASTCommonMatrixType astType = (ASTCommonMatrixType) typeReference.getAstType();
        ASTElementType elementType = astType.getElementType();
        String type = elementType.getName();
        printer.print(type);
        new NumberPrinter(printer).printASTRange(elementType.getRange());
    }

    private void printPortDimension(EMAPortArraySymbol port) {
        MCASTTypeSymbolReference typeReference = (MCASTTypeSymbolReference) port.getTypeReference();
        ASTCommonMatrixType astType = (ASTCommonMatrixType) typeReference.getAstType();
        ASTDimension dimension = astType.getDimension();
        printer.print("^{");
        for (int i = 0; i < dimension.getMatrixDimList().size(); i++) {
            ASTExpression expression = dimension.getMatrixDim(i);
            new NumberPrinter(printer).printASTExpression(expression);

            if (i < dimension.getMatrixDimList().size() - 1)
                printer.print(", ");
        }
        printer.print("}");
    }
}

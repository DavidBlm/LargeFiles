package de.monticore.mlpipelines.automl.emadlprinter;

import conflang._ast.ASTConfLangCompilationUnit;
import conflang._ast.ASTConfigurationEntry;
import conflang._ast.ASTNestedConfigurationEntry;
import conflangliterals.LiteralHelpers;
import conflangliterals._ast.ASTListLiteral;
import conflangliterals._ast.ASTRangeLiteral;
import conflangliterals._ast.ASTTypelessLiteral;
import de.monticore.mcliterals._ast.ASTBooleanLiteral;
import de.monticore.mcliterals._ast.ASTSignedDoubleLiteral;
import de.monticore.mcliterals._ast.ASTSignedIntLiteral;
import de.monticore.mcliterals._ast.ASTSignedLiteral;
import de.monticore.mcliterals._ast.ASTStringLiteral;
import de.monticore.prettyprint.AstPrettyPrinter;
import de.monticore.prettyprint.IndentPrinter;
import java.util.List;

public class ASTConfLangCompilationUnitPrinter implements AstPrettyPrinter<ASTConfLangCompilationUnit> {

    private final IndentPrinter printer;

    public ASTConfLangCompilationUnitPrinter() {
        this.printer = new IndentPrinter();
        this.printer.setIndentLength(4);
    }

    @Override
    public String prettyPrint(ASTConfLangCompilationUnit compilationUnit) {
        String configName = compilationUnit.getConfiguration().getName();

        this.printer.clearBuffer();
        this.printer.println("configuration " + configName + " {");
        this.printer.indent();
        this.printAllConfigurations(compilationUnit);
        this.printer.unindent();
        this.printer.println("}");

        return this.printer.getContent();
    }

    private void printAllConfigurations(ASTConfLangCompilationUnit compilationUnit) {
        for (Object entry: compilationUnit.getConfiguration().getAllConfigurationEntries()) {
            if (entry instanceof ASTNestedConfigurationEntry) {
                this.printNestedConfiguration((ASTNestedConfigurationEntry) entry);
            } else {
                this.printSimpleConfiguration((ASTConfigurationEntry) entry, false);
            }
        }
    }

    private void printSimpleConfiguration(ASTConfigurationEntry configurationEntry, boolean nested) {
        this.printer.print(configurationEntry.getName() + ": ");

        ASTSignedLiteral literal = configurationEntry.getValue();
        if (literal instanceof ASTTypelessLiteral) {
            this.printTypelessLiteral((ASTTypelessLiteral) literal);
        }
        else if (literal instanceof ASTStringLiteral) {
            this.printStringLiteral((ASTStringLiteral) literal);
        }
        else if (literal instanceof ASTSignedIntLiteral) {
            this.printIntLiteral((ASTSignedIntLiteral) literal);
        }
        else if (literal instanceof ASTSignedDoubleLiteral) {
            this.printDoubleLiteral((ASTSignedDoubleLiteral) literal);
        }
        else if (literal instanceof ASTBooleanLiteral) {
            this.printBooleanLiteral((ASTBooleanLiteral) literal);
        }
        else if (literal instanceof ASTListLiteral) {
            this.printListLiteral((ASTListLiteral) literal);
        }
        else if (literal instanceof ASTRangeLiteral) {
            this.printRangeLiteral((ASTRangeLiteral) literal);
        } else {
            throw new IllegalArgumentException("Cannot handle the type of value of ConfigurationEntry: " + literal.getClass().getSimpleName());
        }

        if (nested) {
            this.printer.print(" {");
        }
        this.printer.println("");
    }

    private void printNestedConfiguration(ASTNestedConfigurationEntry nestedConfigurationEntry) {
        List<ASTConfigurationEntry> nestedEntries = nestedConfigurationEntry.getConfigurationEntryList();
        this.printSimpleConfiguration(nestedConfigurationEntry, true);
        this.printer.indent();

        for (ASTConfigurationEntry entry: nestedEntries) {
            this.printSimpleConfiguration(entry, false);
        }

        this.printer.unindent();
        this.printer.println("}");
    }

    private void printTypelessLiteral(ASTTypelessLiteral astTypelessLiteral) {
        this.printer.print(astTypelessLiteral.getValue());
    }
    private void printStringLiteral(ASTStringLiteral astStringLiteral) {
        this.printer.print("\"" + astStringLiteral.getValue() +"\"");
    }

    private void printIntLiteral(ASTSignedIntLiteral astSignedIntLiteral) {
        this.printer.print(astSignedIntLiteral.getValue());
    }

    private void printDoubleLiteral(ASTSignedDoubleLiteral astSignedDoubleLiteral) {
        this.printer.print(astSignedDoubleLiteral.getValue());
    }

    private void printBooleanLiteral(ASTBooleanLiteral astBooleanLiteral) {
        this.printer.print(astBooleanLiteral.getValue());
    }

    private void printListLiteral(ASTListLiteral astListLiteral) {
        this.printer.print(astListLiteral.toString());
    }

    private void printRangeLiteral(ASTRangeLiteral astRangeLiteral) {
        List<ASTSignedLiteral> signedLiterals = astRangeLiteral.getSignedLiteralList();
        String joinedString = signedLiterals.stream().map(LiteralHelpers::literalToString).reduce((a, b) -> a + ":" + b).get();
        this.printer.print("(" + joinedString + ")");
    }


}

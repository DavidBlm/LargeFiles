package de.monticore.symbolmanagement;

import conflang._ast.ASTConfLangCompilationUnit;
import conflang._symboltable.ConfLangLanguage;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._ast.ASTEMACompilationUnit;
import de.monticore.lang.monticar.emadl._symboltable.EMADLLanguage;
import de.monticore.symboltable.MutableScope;
import de.monticore.symboltable.ResolvingConfiguration;
import de.monticore.symboltable.Scope;

public class SymbolTableCreator {

    public static Scope createEMADLSymbolTable(
            ASTEMACompilationUnit astemaCompilationUnit,
            MutableScope enclosingScope) {
        return new EMADLLanguage().getSymbolTableCreator(new ResolvingConfiguration(), enclosingScope)
                .orElseThrow(IllegalStateException::new)
                .createFromAST(astemaCompilationUnit);
    }

    public static Scope createConfLangSymbolTable(
            final ASTConfLangCompilationUnit confLangCompilationUnit,
            final MutableScope enclosingScope) {
        return new ConfLangLanguage().getSymbolTableCreator(new ResolvingConfiguration(), enclosingScope)
                .orElseThrow(IllegalStateException::new)
                .createFromAST(confLangCompilationUnit);
    }
}

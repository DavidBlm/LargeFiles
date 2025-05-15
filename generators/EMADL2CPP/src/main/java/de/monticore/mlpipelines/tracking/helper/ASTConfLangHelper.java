package de.monticore.mlpipelines.tracking.helper;

import conflang._ast.ASTConfLangCompilationUnit;
import conflang._ast.ASTConfigurationEntry;
import conflang._ast.ASTNestedConfigurationEntry;
import conflangliterals._ast.ASTComplexNumberLiteral;
import conflangliterals._ast.ASTRangeLiteral;
import de.monticore.mcliterals._ast.ASTBooleanLiteral;
import de.monticore.mcliterals._ast.ASTCharLiteral;
import de.monticore.mcliterals._ast.ASTDoubleLiteral;
import de.monticore.mcliterals._ast.ASTFloatLiteral;
import de.monticore.mcliterals._ast.ASTIntLiteral;
import de.monticore.mcliterals._ast.ASTLongLiteral;
import de.monticore.mcliterals._ast.ASTNatLiteral;
import de.monticore.mcliterals._ast.ASTNumericLiteral;
import de.monticore.mcliterals._ast.ASTSignedDoubleLiteral;
import de.monticore.mcliterals._ast.ASTSignedFloatLiteral;
import de.monticore.mcliterals._ast.ASTSignedIntLiteral;
import de.monticore.mcliterals._ast.ASTSignedLiteral;
import de.monticore.mcliterals._ast.ASTSignedLongLiteral;
import de.monticore.mcliterals._ast.ASTSignedNatLiteral;
import de.monticore.mcliterals._ast.ASTStringLiteral;
import java.util.HashMap;
import java.util.Map;

public class ASTConfLangHelper {

    public static String getStringValueByKey(ASTNestedConfigurationEntry nestedEntry, String key) {
        ASTConfigurationEntry entry = getConfigurationEntryByKey(nestedEntry, key);
        if(entry == null) {
            return null;
        }
        return getSignedLiteralValue(entry.getValue());
    }

    /**
     * Returns the configuration entry represented by the given key or null if no such entry exists
     */
    public static ASTConfigurationEntry getConfigurationEntryByKey(ASTNestedConfigurationEntry nestedEntry, String key) {
        return nestedEntry.getConfigurationEntryList().stream()
                .filter(entry -> entry.getName().equals(key)).findFirst().orElse(null);
    }

    /**
     * Returns the configuration entry represented by the given key or null if no such entry exists
     */
    public static ASTConfigurationEntry getConfigurationEntryByKey(ASTConfLangCompilationUnit config, String key) {
        return config.getConfiguration().getAllConfigurationEntries().stream()
                .filter(entry -> entry.getName().equals(key)).findFirst().orElse(null);
    }

    /**
     * Returns the value of the given literal as a string
     * @param literal The literal
     * @return Value of the given literal as a string
     */
    public static String getSignedLiteralValue(ASTSignedLiteral literal) {
        if(literal instanceof ASTBooleanLiteral) {
            return ((ASTBooleanLiteral) literal).getValue() ? "true" : "false";
        } else if(literal instanceof ASTCharLiteral){
            return Character.toString(((ASTCharLiteral) literal).getValue());
        } else if(literal instanceof ASTSignedDoubleLiteral) {
            return (Double.toString(((ASTSignedDoubleLiteral) literal).getValue()));
        } else if(literal instanceof ASTSignedIntLiteral){
            return (Integer.toString(((ASTSignedIntLiteral) literal).getValue()));
        } else if(literal instanceof ASTSignedFloatLiteral){
            return (Float.toString(((ASTSignedFloatLiteral) literal).getValue()));
        } else if(literal instanceof ASTSignedLongLiteral){
            return (Long.toString(((ASTSignedLongLiteral) literal).getValue()));
        } else if(literal instanceof ASTSignedNatLiteral){
            return (Integer.toString(((ASTSignedNatLiteral) literal).getValue()));
        } else if(literal instanceof ASTStringLiteral) {
            String value = ((ASTStringLiteral) literal).getValue();
            if (value.isEmpty()) {
                return "\"\"";
            }
            return value;
        } else if(literal instanceof ASTComplexNumberLiteral) {
            ASTComplexNumberLiteral complexNumberLiteral = (ASTComplexNumberLiteral) literal;
            String real = getNumericLiteralValue(complexNumberLiteral.getReal());
            String imaginary = getNumericLiteralValue(complexNumberLiteral.getIm());

            return (complexNumberLiteral.getNegReOpt().isPresent() ? "-"  : "") + real +
                    (complexNumberLiteral.getNegImOpt().isPresent() ? "-" : "+") + imaginary +
                    complexNumberLiteral.getName();
        } else if (literal instanceof ASTRangeLiteral) {
            ASTRangeLiteral rangeLiteral = (ASTRangeLiteral) literal;
            StringBuilder rangeString = new StringBuilder();
            rangeString.append("(");
            for (ASTSignedLiteral signedLiteral : rangeLiteral.getSignedLiteralList()) {
                rangeString.append(getSignedLiteralValue(signedLiteral)).append(":");
            }
            rangeString.deleteCharAt(rangeString.length() - 1);
            rangeString.append(")");
            return rangeString.toString();
        } else {
            return literal.toString(); // ASTTypelessLiteral, ASTListLiteral, ASTRangeLiteral, ASTComponentLiteral
        }
    }

    public static String getNumericLiteralValue(ASTNumericLiteral literal) {
        if(literal instanceof ASTIntLiteral){
            return Integer.toString(((ASTIntLiteral) literal).getValue());
        } else if(literal instanceof ASTDoubleLiteral){
            return Double.toString(((ASTDoubleLiteral) literal).getValue());
        } else if(literal instanceof ASTFloatLiteral){
            return Float.toString(((ASTFloatLiteral) literal).getValue());
        } else if(literal instanceof ASTLongLiteral){
            return Long.toString(((ASTLongLiteral) literal).getValue());
        } else if(literal instanceof ASTNatLiteral){
            return Integer.toString(((ASTNatLiteral) literal).getValue());
        }
        return literal.toString();
    }

    /**
     * Returns a map containing all key-value pairs from the given configuration
     * @param confLangCompilationUnit The configuration
     * @return A map containing all key-value pairs from the given configuration
     */
    public static Map<String, String> getParametersFromConfiguration(ASTConfLangCompilationUnit confLangCompilationUnit) {
        Map<String, String> params = new HashMap<>();
        for(ASTConfigurationEntry entry : confLangCompilationUnit.getConfiguration().getAllConfigurationEntries()) {
            if(entry.isNestedConfiguration()) {
                addNestedParameters(params, entry.getName(), entry);
            } else {
                params.put(entry.getName(), ASTConfLangHelper.getSignedLiteralValue(entry.getValue()));
            }
        }
        return params;
    }

    private static void addNestedParameters(Map<String, String> map, String currentPrefix, ASTConfigurationEntry entry){
        if(entry.isNestedConfiguration()) {
            ASTNestedConfigurationEntry nestedEntry = (ASTNestedConfigurationEntry) entry;

            map.put(currentPrefix, ASTConfLangHelper.getSignedLiteralValue(nestedEntry.getValue()));

            for(ASTConfigurationEntry nested : nestedEntry.getConfigurationEntryList()) {
                addNestedParameters(map, currentPrefix + "." + nested.getName(), nested);
            }
        } else {
            map.put(currentPrefix, ASTConfLangHelper.getSignedLiteralValue(entry.getValue()));
        }
    }

}

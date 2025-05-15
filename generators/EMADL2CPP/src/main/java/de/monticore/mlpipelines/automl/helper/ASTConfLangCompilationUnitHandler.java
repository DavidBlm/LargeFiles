package de.monticore.mlpipelines.automl.helper;

import conflang._ast.ASTConfLangCompilationUnit;
import conflang._ast.ASTConfigurationEntry;
import conflang._ast.ASTNestedConfigurationEntry;
import conflangliterals._ast.ASTRangeLiteral;
import conflangliterals._ast.ASTTypelessLiteral;
import de.monticore.mcliterals._ast.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ASTConfLangCompilationUnitHandler {

    public static Object getValueByKey(ASTConfLangCompilationUnit compilationUnit, String key) {
        List<ASTConfigurationEntry> entries = compilationUnit.getConfiguration().getAllConfigurationEntries();
        ASTConfigurationEntry configurationEntry = entries.stream().filter(entry -> key.equals(entry.getName())).findFirst().orElse(null);
        return getConfigurationEntryValue(configurationEntry.getValue());
    }

    public static ASTConfLangCompilationUnit setValueForKey(ASTConfLangCompilationUnit compilationUnit, String key, Object value) {
        List<ASTConfigurationEntry> entries = compilationUnit.getConfiguration().getAllConfigurationEntries();
        ASTConfigurationEntry configurationEntry = entries.stream().filter(entry -> key.equals(entry.getName())).findFirst().orElse(null);
        setConfigurationEntryValue(configurationEntry, value);
        return compilationUnit;
    }

    public static Map<String, Object> getValuesFromNestedConfiguration(ASTConfLangCompilationUnit compilationUnit, String key) {
        Map<String, Object> configMap = new HashMap<>();

        List<ASTConfigurationEntry> entries = compilationUnit.getConfiguration().getAllConfigurationEntries();
        ASTConfigurationEntry configurationEntry = entries.stream().filter(entry -> key.equals(entry.getName())).findFirst().orElse(null);

        List<ASTConfigurationEntry> nestedEntries = ((ASTNestedConfigurationEntry) configurationEntry).getConfigurationEntryList();
        String rootKey = configurationEntry.getName();
        Object rootValue = getConfigurationEntryValue(configurationEntry.getValue());
        configMap.put(rootKey, rootValue);

        Map<String, Object> nestedConfigMap = new HashMap<>();
        for (ASTConfigurationEntry entry: nestedEntries) {
            String nestedKey = entry.getName();
            Object nestedValue = getConfigurationEntryValue(entry.getValue());
            nestedConfigMap.put(nestedKey, nestedValue);
        }

        configMap.put("nestedMap", nestedConfigMap);
        return configMap;
    }

    public static ASTConfLangCompilationUnit setNestedValueForKeys(ASTConfLangCompilationUnit compilationUnit, String rootKey, String nestedKey, Object value) {
        List<ASTConfigurationEntry> entries = compilationUnit.getConfiguration().getAllConfigurationEntries();
        ASTConfigurationEntry configurationEntry = entries.stream().filter(entry -> rootKey.equals(entry.getName())).findFirst().orElse(null);

        List<ASTConfigurationEntry> nestedEntries = ((ASTNestedConfigurationEntry) configurationEntry).getConfigurationEntryList();
        ASTConfigurationEntry nestedEntry = nestedEntries.stream().filter(entry -> nestedKey.equals(entry.getName())).findFirst().orElse(null);

        setConfigurationEntryValue(nestedEntry, value);
        return compilationUnit;
    }

    public static Map<String, Boolean> getAllKeys(ASTConfLangCompilationUnit compilationUnit) {
        Map<String, Boolean> keysMap = new HashMap<>();
        List<ASTConfigurationEntry> entries = compilationUnit.getConfiguration().getAllConfigurationEntries();

        for (ASTConfigurationEntry entry: entries) {
            String name = entry.getName();
            boolean isNested = (entry instanceof ASTNestedConfigurationEntry);
            keysMap.put(name, isNested);
        }

        return keysMap;
    }

    private static Object getConfigurationEntryValue(Object configurationEntryValue) {
        if (configurationEntryValue instanceof ASTTypelessLiteral) {
            return ((ASTTypelessLiteral) configurationEntryValue).getValue();
        } else if (configurationEntryValue instanceof ASTStringLiteral) {
            return  ((ASTStringLiteral) configurationEntryValue).getValue();
        } else if (configurationEntryValue instanceof ASTSignedIntLiteral) {
            return  ((ASTSignedIntLiteral) configurationEntryValue).getValue();
        } else if (configurationEntryValue instanceof ASTSignedDoubleLiteral) {
            return ((ASTSignedDoubleLiteral) configurationEntryValue).getValue();
        } else if (configurationEntryValue instanceof ASTBooleanLiteral) {
            return ((ASTBooleanLiteral) configurationEntryValue).getValue();
        } else if (configurationEntryValue instanceof ASTRangeLiteral) {
            return getRangeEntryValues((ASTRangeLiteral) configurationEntryValue);
        } else {
            throw new IllegalArgumentException("Cannot handle the type of value of ConfigurationEntry.");
        }
    }

    private static Map<String, Object> getRangeEntryValues(ASTRangeLiteral rangeLiteral) {
        List<ASTSignedLiteral> signedLiterals = rangeLiteral.getSignedLiteralList();
        int listSize = signedLiterals.size();

        Map<String, Object> rangeMap = new HashMap<>();

        Object lower = getConfigurationEntryValue(signedLiterals.get(0));
        rangeMap.put("lower", lower);

        if (listSize == 3) {
            Object stepSize = getConfigurationEntryValue(signedLiterals.get(1));
            rangeMap.put("step_size", stepSize);
        }

        Object upper = getConfigurationEntryValue(signedLiterals.get(listSize - 1));
        rangeMap.put("upper", upper);

        return rangeMap;
    }

    private static void setConfigurationEntryValue(ASTConfigurationEntry configurationEntry, Object newVal) {
        if (configurationEntry.getValue() instanceof ASTTypelessLiteral) {
            if (!(newVal instanceof Double) && !(newVal instanceof Integer)) {
                ((ASTTypelessLiteral) configurationEntry.getValue()).setValue(newVal.toString());
            }
        } else if (configurationEntry.getValue() instanceof ASTSignedIntLiteral) {
            String newValStr = newVal.toString();
            int dotIndex = newValStr.indexOf('.');
            if (dotIndex != -1) {
                newValStr = newValStr.substring(0, dotIndex);
            }
            ((ASTSignedIntLiteral) configurationEntry.getValue()).setSource(newValStr);
        } else if (configurationEntry.getValue() instanceof ASTSignedDoubleLiteral) {
            ((ASTSignedDoubleLiteral) configurationEntry.getValue()).setSource(newVal.toString());
        } else if (configurationEntry.getValue() instanceof ASTBooleanLiteral) {
            if ((boolean) newVal) {
                ((ASTBooleanLiteral) configurationEntry.getValue()).setSource(3);
            } else {
                ((ASTBooleanLiteral) configurationEntry.getValue()).setSource(1);
            }
        } else if (configurationEntry.getValue() instanceof ASTRangeLiteral) {
            List<ASTSignedLiteral> literalList = ((ASTRangeLiteral)configurationEntry.getValue()).getSignedLiteralList();
            Class literalClass = literalList.get(0).getClass();
            ASTSignedLiteral literal = getASTSignedLiteral(newVal, literalClass);
            configurationEntry.setValue(literal);
        } else {
            throw new IllegalArgumentException("Cannot handle the type of value of ConfigurationEntry.");
        }
    }

    private static ASTSignedLiteral getASTSignedLiteral(Object value, Class literalClass) {
        ASTSignedLiteral literal;

        if (literalClass.equals(ASTTypelessLiteral.class)) {
            literal = new ASTTypelessLiteral();
            ((ASTTypelessLiteral) literal).setValue(value.toString());
        } else if (literalClass.equals(ASTStringLiteral.class)) {
            literal = MCLiteralsNodeFactory.createASTStringLiteral();
            ((ASTStringLiteral) literal).setSource(value.toString());
        } else if (literalClass.equals(ASTSignedIntLiteral.class)) {
            literal = MCLiteralsNodeFactory.createASTSignedIntLiteral();
            if (value instanceof Double) {
                value = ((Double) value).intValue();
            }
            ((ASTSignedIntLiteral) literal).setSource(value.toString());
            ((ASTSignedIntLiteral) literal).setNegative((int) value < 0);
        } else if (literalClass.equals(ASTSignedDoubleLiteral.class)) {
            literal = MCLiteralsNodeFactory.createASTSignedDoubleLiteral();
            ((ASTSignedDoubleLiteral) literal).setSource(value.toString());
            ((ASTSignedDoubleLiteral) literal).setNegative((double) value < 0.0);
        } else if (literalClass.equals(ASTBooleanLiteral.class)) {
            literal = MCLiteralsNodeFactory.createASTBooleanLiteral();
            if ((boolean) value) {
                ((ASTBooleanLiteral) literal).setSource(3);
            } else {
                ((ASTBooleanLiteral) literal).setSource(1);
            }
        } else {
            throw new IllegalArgumentException("Cannot handle the type of value of class.");
        }

        return literal;
    }

}

/* (c) https://github.com/MontiCore/monticore */
package de.monticore.lang.monticar.generator.middleware.helpers;

public class NameHelper {
    private NameHelper(){
    }

    public static String getNameTargetLanguage(String name){
        return name
                .replace('.','_')
                .replace('[','_')
                .replace(']','_');
    }

}

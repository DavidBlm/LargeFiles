package de.monticore.lang.monticar.emadl.generator.utils;

import de.se_rwth.commons.logging.Log;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ChecksumGenerator {

    public static String getChecksumForFileSHA1(String s){
        String hash = "";
        Log.info("Start hashing of file " + s, ChecksumGenerator.class.getSimpleName());
        try(FileInputStream inputStream = new FileInputStream(s)) {
            hash = DigestUtils.sha1Hex(inputStream);
        } catch (IOException e){
            e.printStackTrace();
            Log.info("Exception during hashing the file " + s, ChecksumGenerator.class.getSimpleName());
        }
        Log.info("Finished hashing of file " + s, ChecksumGenerator.class.getSimpleName());
        return hash;
    }
}

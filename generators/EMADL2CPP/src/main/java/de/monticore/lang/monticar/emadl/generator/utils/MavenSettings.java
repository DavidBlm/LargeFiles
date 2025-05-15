package de.monticore.lang.monticar.emadl.generator.utils;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.io.xpp3.SettingsXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import de.se_rwth.commons.logging.Log;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MavenSettings {

    @Parameter(defaultValue = "${maven.repo.local}", readonly = true)
    String localRepositoryCLI;

    @Parameter(defaultValue = "${localRepository}", readonly = true)
    ArtifactRepository localRepository;

    @Parameter(defaultValue = "${settings}", readonly = true)
    Settings settings;

    public File getLocalRepository(){
        // Lookup CLI argument
        if(localRepositoryCLI != null){
            Log.info("Found 'maven.repo.local' variable. The value will be used as local maven repository.", this.getClass().getName());
            return new File(localRepositoryCLI);
        }

        // Check mvn localRepository object
        if(localRepository != null){
            Log.info("Found 'localRepository' variable. The value will be used as local maven repository.", this.getClass().getName());
            return new File(localRepository.getBasedir());
        }

        // Check injected maven settings parameter
        if(settings != null){
            Log.info("Found 'settings' variable. The determined value will be used as local maven repository.", this.getClass().getName());
            return new File(settings.getLocalRepository());
        }

        // If no parameters are injected (this can happen depending on the maven version):
        // Check global settings file
        String mvnHomeEnvironment = System.getenv("M2_HOME");
        String mvnHomeSystem = System.getProperty("maven.home");

        if(mvnHomeSystem != null || mvnHomeEnvironment != null) {
            Path mvnHome = Paths.get(mvnHomeSystem != null ? mvnHomeSystem : mvnHomeEnvironment, "conf", "settings.xml");
            try (InputStream in = Files.newInputStream(mvnHome)) {
                Log.info("Read global settings.xml. The determined value will be used as local maven repository.", this.getClass().getName());
                return new File(new SettingsXpp3Reader().read(in).getLocalRepository());
            } catch (IOException e) {
                Log.info("Could not read global maven settings file.", this.getClass().getName());
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                Log.info("Could not parse global maven settings file.", this.getClass().getName());
                e.printStackTrace();
            } catch (NullPointerException e) {
                Log.debug("No localRepository found in your global maven configuration.", this.getClass().getName());
            }
        }

        return Paths.get(System.getProperty("user.home"), ".m2", "repository").toFile();
    }
}

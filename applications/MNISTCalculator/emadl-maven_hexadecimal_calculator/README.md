## Build Dockerimage

1. Check if the Dockerimage imported in the Dockerfile is still supported.

1. Create a personal gitlab access token. Only scope `read_api` is needed! https://git.rwth-aachen.de/-/profile/personal_access_tokens

1. Replace the following lines in the file `settings.xml`

   ```
   <server>
       <id>gitlab-maven</id>
       <configuration>
           <httpHeaders>
               <property>
                   <name>Job-Token</name>
                   <value>${env.CI_JOB_TOKEN}</value>
               </property>
           </httpHeaders>
       </configuration>
   </server>
   ```
  and 
 
   <server>
       <id>gitlab-maven25</id>
       <configuration>
           <httpHeaders>
               <property>
                   <name>Job-Token</name>
                   <value>${env.CI_JOB_TOKEN}</value>
               </property>
           </httpHeaders>
       </configuration>
   </server>
   ...
   with:

   ```
   <server>
        <id>gitlab-maven</id>
        <configuration>
            <httpHeaders>
            <property>
                <name>Private-Token</name>
                <value>${PRIVATE_TOKEN}</value>
            </property>
            </httpHeaders>
        </configuration>
    </server>
   ```
   <server>
       <id>gitlab-maven25</id>
       <configuration>
           <httpHeaders>
               <property>
                   <name>Private-Token</name>
                   <value>${PRIVATE_TOKEN}</value>
               </property>
           </httpHeaders>
       </configuration>
   </server>
   ...

1. Build the image

   ```
   docker build -t registry.git.rwth-aachen.de/monticore/embeddedmontiarc/applications/mnistcalculator/mxnet/run --build-arg PRIVATE_TOKEN=${PRIVATE_TOKEN} .
   ```

   Replace `${PRIVATE_TOKEN}` with your generated token.

   OR:

   Replace ${PRIVATE_TOKEN} with your passcode and run it directly on a Dockerimage in Intellij.

## Run Dockercontainer


1. Start the container:
   ```
   docker run registry.git.rwth-aachen.de/monticore/embeddedmontiarc/applications/mnistcalculator/mxnet/run
   ```


##Changing the configuration
In the Folder src/emadl/calculator you can find a '.conf' and a '.tag'-file.
You can adapt the Training configuration by changing the entries in the '.conf'-file.

You can change the used dataset by:

1. Import the correct dataset in the Maven POM file.
2. Adapt the entry in the '-tag'-file with the Maven identifiers.
3. Adapt the python script.

In each case, the '.streamtest'- file must be updated, as the calculated number of the application will be changed.
In this file, the Tests are separated by the 'tick' keyword. You can change the Output of These Tests based on the error
message from docker, should the streamtest fail.

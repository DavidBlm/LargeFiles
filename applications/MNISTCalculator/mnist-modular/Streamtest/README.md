## Build Dockerimage

1. If you don't have the `mxnet/base` image locally, please navigate to the `docker/base` directory of this repository and build the image first. 

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

   with:

   ```
   <server>
        <id>gitlab-maven</id>
        <configuration>
            <httpHeaders>
            <property>
                <name>Private-Token</name>
                <value>${env.PRIVATE_TOKEN}</value>
            </property>
            </httpHeaders>
        </configuration>
    </server>
   ```

1. Build the image

   ```
   docker build -t registry.git.rwth-aachen.de/monticore/embeddedmontiarc/applications/mnistcalculator/mxnet/run --build-arg PRIVATE_TOKEN=${PRIVATE_TOKEN} .
   ```

   Replace `${PRIVATE_TOKEN}` with your generated token.

## Run Dockercontainer

### Use custom input images

1. Create two directories. One for input files and one for output files.
1. If you like to run the calculator with your custom input, please place exactly 6 files in the input directory.
   The files should have the format: "[0-9].png", where [0-9] is the label of the image.
1. Start the container:
   ```
   docker run -v $(pwd)/input:/build/mnistcalc/emadl-maven-plugin/input -v $(pwd)/output:/build/mnistcalc/emadl-maven-plugin/output registry.git.rwth-aachen.de/monticore/embeddedmontiarc/applications/mnistcalculator/mxnet/run
   ```
1. You should see the output in the command line and a file called img.png in your output directory.

### Use random images from test dataset

1. Start the container:
   ```
   docker run registry.git.rwth-aachen.de/monticore/embeddedmontiarc/applications/mnistcalculator/mxnet/run
   ```

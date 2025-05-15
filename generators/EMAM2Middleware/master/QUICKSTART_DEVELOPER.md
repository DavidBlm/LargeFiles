<!-- (c) https://github.com/MontiCore/monticore -->
# Quickstart guide for generator developers
- Download and install Java(8+), Maven, as well as Git.
- Clone this repository:  
  ```bash
  cd your/project/directory
  git clone https://git.rwth-aachen.de/monticore/EmbeddedMontiArc/generators/EMAM2Middleware.git
  ```
- Build the project
    - Using the command line:  
    ```bash
    cd EMAM2Middleware
    mvn clean install -s settings.xml
    ```
    - Using Intellij Idea:
        - Open and import the project
        - Open the maven settings(ctrl+shift+a, search 'Maven settings'), tick 'Override' at 'User settings file' and select the 'settings.xml' from this Project
        - Update all maven dependencies(ctrl+shift+a, seach 'Reimport All Maven Projects')
        - Build the project(crtl+F9)
- Make your changes

## Compiling the Projects
Options
1. Add your new generated test projects to the integration tests(check the scripts in [src/test/bash/](src/test/bash/) as well as [.gitlab-ci.yml](.gitlab-ci.yml)) and let the CI/CD system compile them. Alternatively you can use Docker to run the integration tests locally(reference [README.md](README.md) , Section 'Running the Integration tests locally')
2. Install all dependencies (reference [README.md](README.md) , Section 'Dependencies needed to compile the generated projects') and execute the generated compile scripts.

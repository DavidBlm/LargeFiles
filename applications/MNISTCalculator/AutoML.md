# AutoML Readme

## Input requirements
- Train config (as configuration symbol )
- Network architecture (as java object)
- Termination criterion (some metric (enum) and some numeric value )
  - i.e. 80% accuracy or 100% precision

## Steps:
1. The AutoML-pipeline gets a start architecture and a start train config as input
2. The AutoML-pipeline calls a training pipeline with the start architecture and the start train config
3. The training pipeline trains the given architecture with the given train config
4. The training pipeline sends the evaluation metrics (i.e. train and validation) to the AutoML-pipeline
5. AutoML-pipeline checks, if a termination criterion is fulfilled
   1. If yes: save last trained architecture 
   2. If no: continue with step 6
6. Adjust architecture and train configuration
7. Call training pipeline with new architecture and new train configuration
8. Continue with step 3

## Training pipeline parameters
The AutoML-pipeline calls the training pipeline with following arguments:
- train config
- architecture
- callback (to get the evaluation metrics)
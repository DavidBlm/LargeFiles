/* (c) https://github.com/MontiCore/monticore */
import Optimizer;

schema General {

    learning_method = supervised: schema {
        supervised, reinforcement, gan, vae;
    }

    context: enum {
        cpu, gpu;
    }

    optimizer: optimizer_type
}
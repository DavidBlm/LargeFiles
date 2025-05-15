/* (c) https://github.com/MontiCore/monticore */

schema EvaluationCriteria {

    metric: enum {
        accuracy, loss, f1;
    }
    acceptance_rate: Q
    max_iteration_number: N
}
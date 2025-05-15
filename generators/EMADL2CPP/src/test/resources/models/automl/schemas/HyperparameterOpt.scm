/* (c) https://github.com/MontiCore/monticore */

schema HyperparameterOpt {

    optimizer_algorithm {
        values:
            SA,
            BO,
            GA,
            PSO,
            Hyperband,
            SH,
            RS;

            define SA {
                initial_temperature = 50.0: Q
            }

            define BO {
                num_random_iter = 5: N
                tradeoff = 0.01: Q
            }

            define GA {
                population_size = 10: N
                selection_rate = 0.6: Q
                crossover_rate = 0.5: Q
                mutation_rate = 0.3: Q
            }

            define PSO {
                population_size = 10: N
                c1 = 2.0: Q
                c2 = 2.0: Q
            }

            define Hyperband {
                max_iter = 9: N
                eta = 3: N
                skip_last = 0: N
            }

            define RS {
                max_iter = 10: N
            }

            define SH {
                max_config = 27: N
                max_iter = 3: N
                eta = 3: N
            }
    }

}
/* (c) https://github.com/MontiCore/monticore */

schema NoiseDistribution {

    noise_distribution_type {
        values: gaussian, uniform;

        define gaussian {
            mean_value: N
            spread_value: N
        }
    }
}
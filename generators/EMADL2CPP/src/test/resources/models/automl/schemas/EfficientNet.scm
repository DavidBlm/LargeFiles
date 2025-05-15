schema EfficientNet extends NeuralArchitectureSearch {

    flops_condition_value: Q
    min_scaling_factors_alpha: Q
    min_scaling_factors_beta: Q
    min_scaling_factors_gamma: Q
    max_scaling_factors_alpha: Q
    max_scaling_factors_beta: Q
    max_scaling_factors_gamma: Q
    scaling_factors_stepsize_alpha: Q
    scaling_factors_stepsize_beta: Q
    scaling_factors_stepsize_gamma: Q
    maximum_image_width_and_height: N
    minimum_image_width_and_height: N
    phi: N

}
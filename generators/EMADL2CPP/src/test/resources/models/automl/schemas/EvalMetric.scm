/* (c) https://github.com/MontiCore/monticore */

schema EvalMetric {

   eval_metric_type {
        values:
            accuracy,
            cross_entropy,
            f1,
            mae,
            mse,
            perplexity,
            rmse,
            top_k_accuracy,
            accuracy_ignore_label,
            bleu;

        define accuracy_ignore_label {
            axis: Z
            metric_ignore_label: Z
        }

        define bleu {
            exclude: Z*
        }
    }
}
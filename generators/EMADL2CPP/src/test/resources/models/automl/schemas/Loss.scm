/* (c) https://github.com/MontiCore/monticore */

schema Loss {

    loss_type {
        values:
            l1,
            l2,
            epe,
            log_cosh,
            sigmoid_binary_cross_entropy,
            huber,
            cross_entropy,
            softmax_cross_entropy,
            softmax_cross_entropy_ignore_indices,
            dice,
            hinge,
            squared_hinge,
            logistic,
            kullback_leibler;

        define huber {
            rho: Q
        }

        define cross_entropy {
            sparse_label: B
            loss_axis: Z
            batch_axis: Z
        }

        define softmax_cross_entropy {
            sparse_label: B
            loss_axis: Z
            batch_axis: Z
            from_logits: B
        }

        define softmax_cross_entropy_ignore_indices {
            sparse_label: B
            loss_axis: Z
            batch_axis: Z
            from_logits: B
            ignore_indices: Z
        }

        define softmax_cross_entropy_ignore_label {
            sparse_label: B
            loss_axis: Z
            batch_axis: Z
            from_logits: B
            loss_ignore_label: Z
        }

        define hinge {
            margin: Q
        }

        define logistic {
            label_format: string
        }

        define kullback_leibler {
            from_logits: B
        }
    }
}
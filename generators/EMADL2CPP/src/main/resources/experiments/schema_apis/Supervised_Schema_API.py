class Supervised_Schema_API:
    def __init__(self):
        self.training_configuration = None
    def set_training_configuration(self,training_configuration):
        self.training_configuration =  training_configuration
    

    

    '''
        possible values:
            rmse
            accuracy
            accuracy_ignore_label
    '''

    def get_eval_metric_value(self):
        return self.training_configuration.get_eval_metric_value()

    #common hyperparameters

    #value-specific hyperparameters
    def get_eval_metric_accuracy_ignore_label_axis(self):
        return self.training_configuration.get_eval_metric_axis()
    def get_eval_metric_accuracy_ignore_label_metric_ignore_label(self):
        return self.training_configuration.get_eval_metric_metric_ignore_label()

    

    '''
        possible values:
            sgd
            adam
            adamw
    '''

    def get_optimizer_value(self):
        return self.training_configuration.get_optimizer_value()

    #common hyperparameters
    def get_optimizer_learning_rate(self):
        return self.training_configuration.get_optimizer_learning_rate()
    def get_optimizer_weight_decay(self):
        return self.training_configuration.get_optimizer_weight_decay()
    def get_optimizer_learning_rate_policy(self):
        return self.training_configuration.get_optimizer_learning_rate_policy()

    #value-specific hyperparameters
    def get_optimizer_sgd_momentum(self):
        return self.training_configuration.get_optimizer_momentum()
    def get_optimizer_adam_beta1(self):
        return self.training_configuration.get_optimizer_beta1()
    def get_optimizer_adam_beta2(self):
        return self.training_configuration.get_optimizer_beta2()
    def get_optimizer_adam_epsilon(self):
        return self.training_configuration.get_optimizer_epsilon()

    

    '''
        possible values:
            cross_entropy
            kullback_leibler
    '''

    def get_loss_value(self):
        return self.training_configuration.get_loss_value()

    #common hyperparameters

    #value-specific hyperparameters
    def get_loss_huber_rho(self):
        return self.training_configuration.get_loss_rho()
    def get_loss_cross_entropy_sparse_label(self):
        return self.training_configuration.get_loss_sparse_label()
    def get_loss_cross_entropy_loss_axis(self):
        return self.training_configuration.get_loss_loss_axis()
    def get_loss_cross_entropy_batch_axis(self):
        return self.training_configuration.get_loss_batch_axis()

    def get_batch_size(self):
        return self.training_configuration.get_batch_size()
    def get_num_epoch(self):
        return self.training_configuration.get_num_epoch()
    def get_checkpoint_period(self):
        return self.training_configuration.get_checkpoint_period()
    def get_preprocessing_name(self):
        return self.training_configuration.get_preprocessing_name()
    def get_load_pretrained(self):
        return self.training_configuration.get_load_pretrained()
    def get_log_period(self):
        return self.training_configuration.get_log_period()
    def get_shuffle_data(self):
        return self.training_configuration.get_shuffle_data()

    def get_context(self):
        return self.training_configuration.get_context()

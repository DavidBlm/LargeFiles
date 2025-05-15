/* (c) https://github.com/MontiCore/monticore */

schema DataImbalance {
    
    imbalance_type {
        values: 
            image_augmentation;
        
        /*  upsampling dataset by data augmentation operations */
        define image_augmentation {
            rotation_angle: Z*
            shift: B 
            scale_in: B 
            scale_out: B
            check_bias: B
        }
    }
}
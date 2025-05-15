/* (c) https://github.com/MontiCore/monticore */

schema Cleaning {
    
    cleaning_type {
        values: 
            remove;
        
        define remove {
            missing: B
            duplicate: B 
            noisy: B 
        }
    }
}
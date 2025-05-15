/* (c) https://github.com/MontiCore/monticore */

schema DataSplitting {
    
    splitting_type {
        values: 
            srs, strs, cs;
        
        /*  sampling algorithms for TVT data splitting */
        define srs {
            tvt_ratio: N*
        }

        define strs {
            tvt_ratio: N*
            strata: string
        }

        define cs {
            tvt_ratio: N*
            sample_size: N1
        }
    }
}
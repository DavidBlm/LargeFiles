/* (c) https://github.com/MontiCore/monticore */

schema ReplayMemory {

    replay_memory_type {
        values: buffer;

        define buffer {
            memory_size: N
            sample_size: N
        }
    }
}
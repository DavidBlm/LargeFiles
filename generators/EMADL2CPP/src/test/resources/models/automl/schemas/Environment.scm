/* (c) https://github.com/MontiCore/monticore */

schema Environment {

    environment_type {
        values: gym, ros_interface;

        define gym {
            name: string
        }

        define ros_interface {
            state: string
            action: string
            reset: string
            terminal: string
            reward: string
        }
    }
}
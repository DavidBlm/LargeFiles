<#-- (c) https://github.com/MontiCore/monticore -->
#pragma once
#include "${compName}.h"
#include <thread>
#include <chrono>
#include "IAdapter_${compName}.h"

class DummyAdapter_${compName}: public IAdapter_${compName}{
	${compName}* component;

public:
	DummyAdapter_${compName}(){

	}

	void tick(){
		cout << "Dummy publish data: component.out1 = "<< component->out1 << endl;
	}

	bool hasReceivedNewData() {
    	return true;
	}
	
	void init(${compName}* comp){
		this->component = comp;
		while(1){
       		    std::this_thread::sleep_for(std::chrono::seconds(1));
		    component->in2 += 1000;
		}
	}

	
};

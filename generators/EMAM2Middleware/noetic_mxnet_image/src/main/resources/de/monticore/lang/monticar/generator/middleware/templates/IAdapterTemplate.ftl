<#-- (c) https://github.com/MontiCore/monticore -->
#pragma once
#include "${compName}.h"

class IAdapter_${compName}{
	public:
		virtual ~IAdapter_${compName}(){}
		virtual void init(${compName}* comp) = 0;
		virtual void tick() = 0;
		virtual bool hasReceivedNewData() = 0;
};

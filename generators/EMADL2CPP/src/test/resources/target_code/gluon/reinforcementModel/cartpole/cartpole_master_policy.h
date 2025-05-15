/* (c) https://github.com/MontiCore/monticore */
#ifndef CARTPOLE_MASTER_POLICY
#define CARTPOLE_MASTER_POLICY
#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif
#include "armadillo"
using namespace arma;
class cartpole_master_policy{
public:
colvec values;
int action;
void init()
{
values=colvec(2);
}
void execute()
{
double maxQValue = values(1-1);
int maxValueAction = 0;
for( auto i=1;i<=2;++i){
if((values(i-1) > maxQValue)){
maxQValue = values(i-1);
maxValueAction = i-1;
}
}
action = maxValueAction;
}

};
#endif

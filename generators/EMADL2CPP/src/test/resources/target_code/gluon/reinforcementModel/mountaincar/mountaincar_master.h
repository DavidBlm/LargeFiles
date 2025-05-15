#ifndef MOUNTAINCAR_MASTER
#define MOUNTAINCAR_MASTER
#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif
#include "armadillo"
#include "mountaincar_master_actor.h"
using namespace arma;
class mountaincar_master{
public:
colvec state;
colvec action;
mountaincar_master_actor actor;
void init()
{
state=colvec(2);
action=colvec(1);
actor.init();
}
void execute()
{
actor.state = state;
actor.execute();
action = actor.action;
}

};
#endif

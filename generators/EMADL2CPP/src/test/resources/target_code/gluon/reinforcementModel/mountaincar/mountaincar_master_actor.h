#ifndef MOUNTAINCAR_MASTER_ACTOR
#define MOUNTAINCAR_MASTER_ACTOR
#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif
#include "armadillo"
#include "CNNPredictor_mountaincar_master_actor.h"
#include "CNNTranslator.h"
using namespace arma;
class mountaincar_master_actor{
public:
CNNPredictor_mountaincar_master_actor_0 _predictor_0_;
colvec state;
colvec action;
void init()
{
state=colvec(2);
action=colvec(1);
}
void execute(){
    vector<float> state_ = CNNTranslator::translate(state);

    vector<float> action_(1);


    _predictor_0_.predict(state_, action_);

    action = CNNTranslator::translateToCol(action_, std::vector<size_t> {1});

}

};
#endif

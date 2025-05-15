<#-- (c) https://github.com/MontiCore/monticore -->
#include <iostream>
#include <thread>
#include <chrono>
#include <atomic>
#include <list>
#include "IAdapter_${compName}.h"

${includes}

using namespace std;
using namespace chrono;

static int exeMs = 100;

bool executeOnDemand = false;

bool parseCommandLineParameters(int argc, char* argv[]){
  if(argc == 1){
    return true;
  }

  for (int i = 1; i < argc; i++) {
    if(string(argv[i]) == "-executeOnDemand" ||
       string(argv[i]) == "-xod"
      )
    {
      executeOnDemand = true;
      return true;
    }
  }

  if(argc == 3 && string(argv[1]) == "-t"){
    try{
      int tmp = stoi(argv[2]);
      if(tmp >= 0){
        exeMs = tmp;
        return true;
      }
    }catch(...){
      //Ignore
    }
  }
  cout << "Usage: " << argv[0] << " [-h | -t sleepTimeMs]\n";
  return false;
}

void startMiddleware(IAdapter_${compName}& adapter,${compName}& comp,atomic<bool>& done){
  adapter.init(&comp);
  done = true;
}

int main(int argc, char* argv[])
{
  if(!parseCommandLineParameters(argc,argv)){
    return 1;
  }

  atomic<bool> done(false);
  ${compName} comp;
  comp.init(${initParams});

  list<IAdapter_${compName}*> adapters;
  ${addAdapters}

  list<thread*> threads;
  for(auto a : adapters){
    threads.push_back(new thread(startMiddleware,ref(*a),ref(comp),ref(done)));
  }

  cout << "waiting for all middleware to start\n";
  this_thread::sleep_for(seconds(3));
  
  if (executeOnDemand) {
    cout << "started! Execute only on demand\n";
  } else {
    cout << "started! Executing every " << exeMs << "ms\n";
  }

  time_point<system_clock> start, end;
  bool hasReceivedNewData = true;

  while(!done){
    start = system_clock::now();

    if (executeOnDemand) {
      
      hasReceivedNewData = true;
      
      for(auto a : adapters){
        if ((*a).hasReceivedNewData() == false) {
          hasReceivedNewData = false;
          break;
        }
      }

      if (hasReceivedNewData) {
        comp.execute();
        for(auto a : adapters){
          (*a).tick();
        }
      }

    } else {
      comp.execute();
      for(auto a : adapters){
        (*a).tick();
      }
      end = system_clock::now();
      int elapsedMs = duration_cast<milliseconds>(end-start).count();
      int newSleep = exeMs - elapsedMs;
      if(newSleep <= 0){
        cout << "Cant keep up! "<< (-newSleep) <<"ms late!\n";
      }else{
        this_thread::sleep_for(milliseconds(newSleep));
      }

    }
    
  }

  return 0;
}

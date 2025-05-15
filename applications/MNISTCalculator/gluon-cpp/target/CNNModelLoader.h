#ifndef CNNMODELLOADER
#define CNNMODELLOADER

#include <mxnet-cpp/MxNetCpp.h>

#include <stdio.h>
#include <iostream>
#include <fstream>

using namespace mxnet::cpp;

// Read files to load moddel symbol and parameters
class ModelLoader {
private:
    Context ctx = Context::cpu();
    std::vector<Symbol> network_symbol_list;
    std::vector<std::map<std::string, NDArray>> network_param_map_list;

    std::vector<Symbol> query_symbol_list;
    std::vector<std::map<std::string, NDArray>> query_param_map_list;

    std::vector<std::map<std::string, NDArray>> replay_memory;

    std::vector<Symbol> loss_symbol;
    std::vector<std::map<std::string, NDArray>> loss_param_map;


    void checkFile(std::string file_path){
        std::ifstream ifs(file_path.c_str(), std::ios::in | std::ios::binary);
        if (!ifs) {
            std::cerr << "Can't open the file. Please check " << file_path << ". \n";
            return;
        }

        int length_;
        ifs.seekg(0, std::ios::end);
        length_ = ifs.tellg();
        ifs.seekg(0, std::ios::beg);
        std::cout << file_path.c_str() << " ... "<< length_ << " bytes\n";
        ifs.close();
    }

    void loadComponent(std::string json_path,
                       std::string param_path,
                       std::vector<Symbol> &symbols_list,
                       std::vector<std::map<std::string, NDArray>> &param_map_list){
        checkFile(json_path);
        symbols_list.push_back(Symbol::Load(json_path));
        checkFile(param_path);
        std::map<std::string, NDArray> params;
        NDArray::Load(param_path, 0, &params);
        param_map_list.push_back(processParamMap(params));
    }

    std::map<std::string, NDArray> processParamMap(std::map<std::string, NDArray> param_map){
        std::map<std::string, NDArray> processed_param_map;
        if(!param_map.empty()){
            for (const auto &pair : param_map) {
                std::string name = pair.first.substr(4); //the first four letters would be the type (arg: or aux:, but we don't have aux parameters? <- need to make sure)
                processed_param_map[name] = pair.second.Copy(ctx);
            }
        }
        return processed_param_map;
    }

public:
    explicit ModelLoader(std::string file_prefix, mx_uint num_subnets, Context ctx_param){

        ctx = ctx_param;
        std::string network_json_path;
        std::string network_param_path;
        std::string query_json_path;
        std::string query_param_path;
        std::string memory_path;
        std::string loss_json_path;
        std::string loss_param_path;

        //Load network
        if(!num_subnets){
            network_json_path = file_prefix + "-symbol.json";
            network_param_path = file_prefix + "-0000.params";
            loadComponent(network_json_path, network_param_path, network_symbol_list, network_param_map_list);
        }else{
            for(int i=0; i < num_subnets; i++){
                network_json_path = file_prefix + "_episodic_sub_net_" + std::to_string(i) + "-symbol.json";
                network_param_path = file_prefix + "_episodic_sub_net_" + std::to_string(i) + "-0000.params";
                loadComponent(network_json_path, network_param_path, network_symbol_list, network_param_map_list);
                if(i >= 1){
                    query_json_path = file_prefix + "_episodic_query_net_" + std::to_string(i) + "-symbol.json";
                    query_param_path = file_prefix + "_episodic_query_net_" + std::to_string(i) + "-0000.params";
                    loadComponent(query_json_path, query_param_path, query_symbol_list, query_param_map_list);
                    
                    memory_path = file_prefix + "_episodic_memory_sub_net_" + std::to_string(i) + "-0000";
                    checkFile(memory_path);

                    std::map<std::string, NDArray> mem_map = NDArray::LoadToMap(memory_path);
                    for(auto &mem : mem_map){
                        mem.second = mem.second.Copy(ctx);
                    }
                    replay_memory.push_back(mem_map);
                }
            }
        }

        //Load Loss
        loss_json_path = file_prefix + "_loss-symbol.json";
        loss_param_path = file_prefix + "_loss-0000.params";
        loadComponent(loss_json_path, loss_param_path, loss_symbol, loss_param_map);

        NDArray::WaitAll();
    }

    std::vector<Symbol> GetNetworkSymbols() {
        return network_symbol_list;
    }

    std::vector<std::map<std::string, NDArray>> GetNetworkParamMaps() {
        return network_param_map_list;
    }

    Symbol GetLoss() {
        return loss_symbol[0];
    }

    std::map<std::string, NDArray> GetLossParamMap() {
        return loss_param_map[0];
    }

    std::vector<Symbol> GetQuerySymbols() {
        return query_symbol_list;
    }

    std::vector<std::map<std::string, NDArray>>  GetQueryParamMaps() {
        return query_param_map_list;
    }

    std::vector<std::map<std::string, NDArray>> GetReplayMemory(){
        return replay_memory;
    }
};
#endif // CNNMODELLOADER

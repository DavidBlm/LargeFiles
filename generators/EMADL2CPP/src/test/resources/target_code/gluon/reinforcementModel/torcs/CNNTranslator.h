/* (c) https://github.com/MontiCore/monticore */
#ifndef CNNTRANSLATOR_H
#define CNNTRANSLATOR_H
#include <armadillo>
#include <cassert>
#include <vector>
#include <utility>
#include <algorithm>
#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>

using namespace std;
using namespace arma;

class CNNTranslator{
public:
    template<typename T> static void addColToSTDVector(const Col<T> &source, vector<float> &data){
        for(size_t i = 0; i < source.n_elem; i++){
            data.push_back((float) source(i));
        }
    }

    template<typename T> static void addRowToSTDVector(const subview_row<T> &source, vector<float> &data){
        for(size_t i = 0; i < source.n_elem; i++){
            data.push_back((float) source(i));
        }
    }

    template<typename T> static void addRowToSTDVector(const Row<T> &source, vector<float> &data){
        for(size_t i = 0; i < source.n_elem; i++){
            data.push_back((float) source(i));
        }
    }

    template<typename T> static void addMatToSTDVector(const Mat<T> &source, vector<float> &data){
        for(size_t i = 0; i < source.n_rows; i++){
            addRowToSTDVector(source.row(i), data);
        }
    }


    template<typename T> static vector<float> translate(const Col<T> &source){
        size_t size = source.n_elem;
        vector<float> data;
        data.reserve(size);
        addColToSTDVector(source, data);
        return data;
    }

    template<typename T> static vector<float> translate(const Row<T> &source){
        size_t size = source.n_elem;
        vector<float> data;
        data.reserve(size);
        addRowToSTDVector(source, data);
        return data;
    }

    template<typename T> static vector<float> translate(const Mat<T> &source){
        size_t size = source.n_elem;
        vector<float> data;
        data.reserve(size);
        addMatToSTDVector(source, data);
        return data;
    }

    template<typename T> static vector<float> translate(const Cube<T> &source){
        size_t size = source.n_elem;
        vector<float> data;
        data.reserve(size);
        for(size_t i = 0; i < source.n_slices; i++){
            addMatToSTDVector(source.slice(i), data);
        }
        return data;
    }

    static vec translateToCol(const vector<float> &source, const vector<size_t> &shape){
        assert(shape.size() == 1);
        vec column(shape[0]);
        for(size_t i = 0; i < source.size(); i++){
            column(i) = (double) source[i];
        }
        return column;
    }

    static mat translateToMat(const vector<float> &source, const vector<size_t> &shape){
        assert(shape.size() == 2);
        mat matrix(shape[1], shape[0]); //create transposed version of the matrix
        int startPos = 0;
        int endPos = matrix.n_rows;
        const vector<size_t> columnShape = {matrix.n_rows};
        for(size_t i = 0; i < matrix.n_cols; i++){
            vector<float> colSource(&source[startPos], &source[endPos]);
            matrix.col(i) = translateToCol(colSource, columnShape);
            startPos = endPos;
            endPos += matrix.n_rows;
        }
        return matrix.t();
    }

    static cube translateToCube(const vector<float> &source, const vector<size_t> &shape){
        assert(shape.size() == 3);
        cube cubeMatrix(shape[1], shape[2], shape[0]);
        const int matrixSize = shape[1] * shape[2];
        const vector<size_t> matrixShape = {shape[1], shape[2]};
        int startPos = 0;
        int endPos = matrixSize;
        for(size_t i = 0; i < cubeMatrix.n_slices; i++){
            vector<float> matrixSource(&source[startPos], &source[endPos]);
            cubeMatrix.slice(i) = translateToMat(matrixSource, matrixShape);
            startPos = endPos;
            endPos += matrixSize;
        }
        return cubeMatrix;
    }

    static ivec translateToIntCol(const vector<float> &source, const vector<size_t> &shape){
        assert(shape.size() == 1);
        ivec column(shape[0]);
        for(size_t i = 0; i < source.size(); i++){
            column(i) = (size_t) source[i];
        }
        return column;
    }

    static imat translateToIntMat(const vector<float> &source, const vector<size_t> &shape){
        assert(shape.size() == 2);
        imat matrix(shape[1], shape[0]); //create transposed version of the matrix
        int startPos = 0;
        int endPos = matrix.n_rows;
        const vector<size_t> columnShape = {matrix.n_rows};
        for(size_t i = 0; i < matrix.n_cols; i++){
            vector<float> colSource(&source[startPos], &source[endPos]);
            matrix.col(i) = translateToIntCol(colSource, columnShape);
            startPos = endPos;
            endPos += matrix.n_rows;
        }
        return matrix.t();
    }

    static icube translateToIntCube(const vector<float> &source, const vector<size_t> &shape){
        assert(shape.size() == 3);
        icube cubeMatrix(shape[1], shape[2], shape[0]);
        const int matrixSize = shape[1] * shape[2];
        const vector<size_t> matrixShape = {shape[1], shape[2]};
        int startPos = 0;
        int endPos = matrixSize;
        for(size_t i = 0; i < cubeMatrix.n_slices; i++){
            vector<float> matrixSource(&source[startPos], &source[endPos]);
            cubeMatrix.slice(i) = translateToIntMat(matrixSource, matrixShape);
            startPos = endPos;
            endPos += matrixSize;
        }
        return cubeMatrix;
    }

    static icube translateImageToICube(cv::Mat img, const int slices, const int rows, const int cols) {
        cv::Size scale(cols,rows);
        cv::resize(img, img, scale);
        std::cout << "== simply resize: " << img.size() << " ==" << std::endl;

        size_t channels = slices;
        size_t height = img.rows;
        size_t width = img.cols;
        vector<float> data(channels*height*width);

        for(size_t j=0; j<height; j++){
            for(size_t k=0; k<width; k++){
                cv::Vec3b intensity = img.at<cv::Vec3b>(j, k);
                for(size_t i=0; i<channels; i++){
                    data[i*height*width + j*height + k] = (float) intensity[i];
                }
            }
        }
        return conv_to< icube >::from( translateToCube(data, vector<size_t> {channels,height,width}) );
    }

    template<typename T> static vector<size_t> getShape(const Col<T> &source){
        return {source.n_elem};
    }

    template<typename T> static vector<size_t> getShape(const Row<T> &source){
        return {source.n_elem};
    }

    template<typename T> static vector<size_t> getShape(const Mat<T> &source){
        return {source.n_rows, source.n_cols};
    }

    template<typename T> static vector<size_t> getShape(const Cube<T> &source){
        return {source.n_slices, source.n_rows, source.n_cols};
    }
};

#endif
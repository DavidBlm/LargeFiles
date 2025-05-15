#ifndef HELPERA_H
#define HELPERA_H
#include <iostream>
#include "armadillo"
#include <stdarg.h>
#include <initializer_list>
#include <fstream>
using namespace arma;
#ifndef _FILESTRING_CONVERSION___A
#define _FILESTRING_CONVERSION___A
void toFileString(std::ofstream& myfile, mat A){
    myfile << "[";
    for (int i = 0; i < A.n_rows; i++){
        for (int j = 0; j < A.n_cols; j++){
            myfile << A(i,j);
            if(j + 1 < A.n_cols){
                myfile << ", ";
            }
        }
        if(i + 1 < A.n_rows){
            myfile << ";";
        }
    }
    myfile << "]";
}
void toFileString(std::ofstream& myfile, double A){
    myfile << A;
}
void toFileString(std::ofstream& myfile, float A){
    myfile << A;
}
void toFileString(std::ofstream& myfile, int A){
    myfile << A;
}
void toFileString(std::ofstream& myfile, bool A){
    myfile << A;
}
bool Is_close(mat& X, mat& Y, double tol)
{
    // abs returns a mat type then max checks columns and returns a row_vec
    // max used again will return the biggest element in the row_vec
    bool close(false);
    if(arma::max(arma::max(arma::abs(X-Y))) < tol)
    {
        close = true;
    }
    return close;
}
#endif
class HelperA{
public:
static mat getEigenVectors(mat A){
vec eigenValues;
mat eigenVectors;
eig_sym(eigenValues,eigenVectors,A);
return eigenVectors;
}
static vec getEigenValues(mat A){
vec eigenValues;
mat eigenVectors;
eig_sym(eigenValues,eigenVectors,A);
return eigenValues;
}

static mat getKMeansClusters(mat A, int k){
mat clusters;
kmeans(clusters,A.t(),k,random_subset,20,true);
/*printf("cluster centroid calculation done\n");
std::ofstream myfile;
     myfile.open("data after cluster.txt");
     myfile << A;
     myfile.close();
	 
	 std::ofstream myfile2;
     myfile2.open("cluster centroids.txt");
     myfile2 << clusters;
     myfile2.close();*/
mat indexedData=getKMeansClustersIndexData(A.t(), clusters);

/*std::ofstream myfile3;
     myfile3.open("data after index.txt");
     myfile3 << indexedData;
     myfile3.close();
	 */
return indexedData;
}

static mat getKMeansClustersIndexData(mat A, mat centroids){
	mat result=mat(A.n_cols, 1);
	for(int i=0;i<A.n_cols;++i){
		result(i, 0) = getIndexForClusterCentroids(A, i, centroids);
	}
	return result;
}

static int getIndexForClusterCentroids(mat A, int colIndex, mat centroids){
	int index=1;
	double lowestDistance=getEuclideanDistance(A, colIndex, centroids, 0);
	for(int i=1;i<centroids.n_cols;++i){
		double curDistance=getEuclideanDistance(A, colIndex, centroids, i);
		if(curDistance<lowestDistance){
			lowestDistance=curDistance;
			index=i+1;
		}
	}
	return index;
}

static double getEuclideanDistance(mat A, int colIndexA, mat B, int colIndexB){
	double distance=0;
	for(int i=0;i<A.n_rows;++i){
		double elementA=A(i,colIndexA);
		double elementB=B(i,colIndexB);
		double diff=elementA-elementB;
		distance+=diff*diff;
	}
	return sqrt(distance);
}

static mat getSqrtMat(mat A){
    cx_mat result=sqrtmat(A);
    return real(result);
}

static mat getSqrtMatDiag(mat A){
for(int i=0;i<A.n_rows;++i){
    double curVal = A(i,i);
    A(i,i) = sqrt(curVal);
}
return A;
}

static mat invertDiagMatrix(mat A){
for(int i=0;i<A.n_rows;++i){
    double curVal = A(i,i);
    A(i,i) = 1/curVal;
}
return A;
}
};
#endif

# (c) https://github.com/MontiCore/monticore  
import json
import matplotlib.pyplot as plt
import argparse


def visualise_silhouette_result(filename, modelName):
    with open(filename) as data_file:
        data_raw = json.load(data_file)

    data = []

    for d in data_raw:
        data.append((d["NumberOfClusters"], d["Score"]))

    #sort by cluster size
    data.sort(key=lambda d: d[0])
    clusters = [cluster for (cluster, score) in data]
    score = [score for (cluster, score) in data]

    min_score = min(score)
    max_score = max(score)

    #plot
    fig, ax = plt.subplots()
    ax.plot(clusters, score, marker='.', linestyle='none')
    ax.set_ylim(bottom=-1, top=1)
    ax.set(xlabel='Cluster size', ylabel='Silhouette score',
           title='Silhouette score of the ' + modelName + ' model ordered by clustering size')
    ax.grid()
    textstr = '\n'.join((
        "min = " + str(round(min_score, 3)),
        "max= " + str(round(max_score, 3))))
    props = dict(boxstyle='round', facecolor='wheat', alpha=0.5)
    ax.text(0.95, 0.05, textstr, transform=ax.transAxes, fontsize=14,
            verticalalignment='bottom', ha='right', bbox=props)
    plt.show()


def parse_args():
    parser = argparse.ArgumentParser(description='Get json files')
    parser.add_argument('autopilot', metavar='autopilot',
                        help='montecarloresult json location of autopilot')
    parser.add_argument('pacman', metavar='pacman',
                        help='clusteringResults.json location of pacman')
    parser.add_argument('supermario', metavar='supermario',
                        help='clusteringResults.json location of supermario')
    parser.add_argument('daimler', metavar='daimler',
                        help='clusteringResults.json location of daimlermodel')
    return parser.parse_args()


args = parse_args()
visualise_silhouette_result(args.autopilot, "Autopilot")
visualise_silhouette_result(args.pacman, "Pacman")
visualise_silhouette_result(args.supermario, "Supermario")
visualise_silhouette_result(args.daimler, "Daimler")

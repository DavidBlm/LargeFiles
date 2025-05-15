# (c) https://github.com/MontiCore/monticore  
import json
import numpy as np
import matplotlib.pyplot as plt
import argparse
import math

parser = argparse.ArgumentParser(description='Get json files')
parser.add_argument('autopilot', metavar='autopilot',
                    help='clusteringResults.json location of autopilot')
parser.add_argument('pacman', metavar='pacman',
                    help='clusteringResults.json location of pacman')
parser.add_argument('supermario', metavar='supermario',
                    help='clusteringResults.json location of supermario')
parser.add_argument('daimler', metavar='daimler',
                    help='clusteringResults.json location of daimlermodel')

args = parser.parse_args()


with open(args.autopilot) as data_file:
    autopilot_data = json.load(data_file)

with open(args.pacman) as data_file:
    pacman_data = json.load(data_file)

with open(args.supermario) as data_file:
    supermario_data = json.load(data_file)

with open(args.daimler) as data_file:
    daimler_data = json.load(data_file)

autopilot_algorithmdata = []
autopilot_clusterdata = []
autopilot_scoredata = []
autopilot_durationdata = []
autopilot_componentNumber = 0
i = 0
while i < len(autopilot_data):
    autopilot_algorithmdata.append(autopilot_data[i]["Algorithm"])
    autopilot_clusterdata.append(autopilot_data[i]["NumberOfClusters"])
    autopilot_scoredata.append(int(autopilot_data[i]["Score"]))
    autopilot_durationdata.append(autopilot_data[i]["DurationInMs"])
    if i == 0:
        autopilot_componentNumber = autopilot_data[i]["ComponentNumber"]
    i = i + 1
autopilot_clusterdata.append(autopilot_componentNumber)
	
pacman_algorithmdata = []
pacman_clusterdata = []
pacman_scoredata = []
pacman_durationdata = []
pacman_componentNumber = 0
i = 0
while i < len(pacman_data):
    pacman_algorithmdata.append(pacman_data[i]["Algorithm"])
    pacman_clusterdata.append(pacman_data[i]["NumberOfClusters"])
    pacman_scoredata.append(int(pacman_data[i]["Score"]))
    pacman_durationdata.append(pacman_data[i]["DurationInMs"])
    if i == 0:
	    pacman_componentNumber = pacman_data[i]["ComponentNumber"]
    i = i + 1
pacman_clusterdata.append(pacman_componentNumber)

supermario_algorithmdata = []
supermario_clusterdata = []
supermario_scoredata = []
supermario_durationdata = []
supermario_componentNumber = 0
i = 0
while i < len(supermario_data):
    supermario_algorithmdata.append(supermario_data[i]["Algorithm"])
    supermario_clusterdata.append(supermario_data[i]["NumberOfClusters"])
    supermario_scoredata.append(int(supermario_data[i]["Score"]))
    supermario_durationdata.append(supermario_data[i]["DurationInMs"])
    if i == 0:
	    supermario_componentNumber = supermario_data[i]["ComponentNumber"]
    i = i + 1
supermario_clusterdata.append(supermario_componentNumber)

daimler_algorithmdata = []
daimler_clusterdata = []
daimler_scoredata = []
daimler_durationdata = []
daimler_componentNumber = 0
i = 0
while i < len(daimler_data):
    daimler_algorithmdata.append(daimler_data[i]["Algorithm"])
    daimler_clusterdata.append(daimler_data[i]["NumberOfClusters"])
    daimler_scoredata.append(int(daimler_data[i]["Score"]))
    daimler_durationdata.append(daimler_data[i]["DurationInMs"])
    if i == 0:
        daimler_componentNumber = daimler_data[i]["ComponentNumber"]
    i = i + 1
daimler_clusterdata.append(daimler_componentNumber)

cluster_dataspectral1 = (autopilot_data[0]["NumberOfClusters"], pacman_data[0]["NumberOfClusters"], supermario_data[0]["NumberOfClusters"], daimler_data[0]["NumberOfClusters"])
cluster_dataspectral2 = (autopilot_data[1]["NumberOfClusters"], pacman_data[1]["NumberOfClusters"], supermario_data[1]["NumberOfClusters"], daimler_data[1]["NumberOfClusters"])
cluster_dataspectral3 = (autopilot_data[2]["NumberOfClusters"], pacman_data[2]["NumberOfClusters"], supermario_data[2]["NumberOfClusters"], daimler_data[2]["NumberOfClusters"])
cluster_datamarkov    = (autopilot_data[3]["NumberOfClusters"], pacman_data[3]["NumberOfClusters"], supermario_data[3]["NumberOfClusters"], daimler_data[3]["NumberOfClusters"])
cluster_dataaffinity  = (autopilot_data[4]["NumberOfClusters"], pacman_data[4]["NumberOfClusters"], supermario_data[4]["NumberOfClusters"], daimler_data[4]["NumberOfClusters"])

score_dataspectral1 = (int(autopilot_data[0]["Score"]), int(pacman_data[0]["Score"]), int(supermario_data[0]["Score"]), int(daimler_data[0]["Score"]))
score_dataspectral2 = (int(autopilot_data[1]["Score"]), int(pacman_data[1]["Score"]), int(supermario_data[1]["Score"]), int(daimler_data[1]["Score"]))
score_dataspectral3 = (int(autopilot_data[2]["Score"]), int(pacman_data[2]["Score"]), int(supermario_data[2]["Score"]), int(daimler_data[2]["Score"]))
score_datamarkov    = (int(autopilot_data[3]["Score"]), int(pacman_data[3]["Score"]), int(supermario_data[3]["Score"]), int(daimler_data[3]["Score"]))
score_dataaffinity  = (int(autopilot_data[4]["Score"]), int(pacman_data[4]["Score"]), int(supermario_data[4]["Score"]), int(daimler_data[4]["Score"]))

duration_dataspectral1 = (autopilot_data[0]["DurationInMs"], pacman_data[0]["DurationInMs"], supermario_data[0]["DurationInMs"], daimler_data[0]["DurationInMs"])
duration_dataspectral2 = (autopilot_data[1]["DurationInMs"], pacman_data[1]["DurationInMs"], supermario_data[1]["DurationInMs"], daimler_data[1]["DurationInMs"])
duration_dataspectral3 = (autopilot_data[2]["DurationInMs"], pacman_data[2]["DurationInMs"], supermario_data[2]["DurationInMs"], daimler_data[2]["DurationInMs"])
duration_datamarkov    = (autopilot_data[3]["DurationInMs"], pacman_data[3]["DurationInMs"], supermario_data[3]["DurationInMs"], daimler_data[3]["DurationInMs"])
duration_dataaffinity  = (autopilot_data[4]["DurationInMs"], pacman_data[4]["DurationInMs"], supermario_data[4]["DurationInMs"], daimler_data[4]["DurationInMs"])

componentNumbers = (autopilot_componentNumber, pacman_componentNumber, supermario_componentNumber, daimler_componentNumber)

def autolabel(rects, xpos='center'):
    """
    Attach a text label above each bar in *rects*, displaying its height.

    *xpos* indicates which side to place the text w.r.t. the center of
    the bar. It can be one of the following {'center', 'right', 'left'}.
    """

    xpos = xpos.lower()  # normalize the case of the parameter
    ha = {'center': 'center', 'right': 'left', 'left': 'right'}
    offset = {'center': 0.5, 'right': 0.57, 'left': 0.43}  # x_txt = x + w*off

    for rect in rects:
        height = rect.get_height()
        ax.text(rect.get_x() + rect.get_width()*offset[xpos], height,
                '{}'.format(height), ha=ha[xpos], va='top', rotation=90)

#----------------------------------------------------------------------------
ind = np.arange(len(autopilot_data) + 1)  # the x locations for the groups
width = 0.2  # the width of the bars

fig, ax = plt.subplots()
rects1 = ax.bar(ind - 1.5 * width, autopilot_clusterdata, width,
                color='SkyBlue', label='Autopilot')
rects2 = ax.bar(ind - 0.5 * width, pacman_clusterdata, width,
                color='Red', label='Pacman')
rects3 = ax.bar(ind + 0.5 * width, supermario_clusterdata, width,
                color='Green', label='SuperMario')
rects4 = ax.bar(ind + 1.5 * width, daimler_clusterdata, width,
                color='Orange', label='DaimlerModel')

# Add some text for labels, title and custom x-axis tick labels, etc.
ax.set_ylabel('Number of Clusters')
ax.set_title('Number of Clusters')
ax.set_xticks(ind)
ax.set_xticklabels(('Spectral','Spectral','Spectral','Markov','Affinity','NumComp'))
ax.legend(loc='best')

autolabel(rects1, "center")
autolabel(rects2, "center")
autolabel(rects3, "center")
autolabel(rects4, "center")

plt.yscale("log")
plt.show()

#----------------------------------------------------------------------------------------
ind = np.arange(len(autopilot_data))  # the x locations for the groups
width = 0.2  # the width of the bars

fig, ax = plt.subplots()
rects1 = ax.bar(ind - 1.5 * width, autopilot_scoredata, width,
                color='SkyBlue', label='Autopilot')
rects2 = ax.bar(ind - 0.5 * width, pacman_scoredata, width,
                color='Red', label='Pacman')
rects3 = ax.bar(ind + 0.5 * width, supermario_scoredata, width,
                color='Green', label='SuperMario')
rects4 = ax.bar(ind + 1.5 * width, daimler_scoredata, width,
                color='Orange', label='DaimlerModel')

# Add some text for labels, title and custom x-axis tick labels, etc.
ax.set_ylabel('Score')
ax.set_title('Score after Heuristic', y = -0.13)
ax.set_xticks(ind)
ax.set_xticklabels(('Spectral','Spectral','Spectral','Markov','Affinity'))
ax.legend(bbox_to_anchor=(0., 1.02, 1., .102), loc=3,
       ncol=2, mode="expand", borderaxespad=0.)

autolabel(rects1, "center")
autolabel(rects2, "center")
autolabel(rects3, "center")
autolabel(rects4, "center")

plt.yscale("log")
plt.show()

#-----------------------------------------------------------------------------------
ind = np.arange(len(autopilot_data))  # the x locations for the groups
width = 0.2  # the width of the bars

fig, ax = plt.subplots()
rects1 = ax.bar(ind - 1.5 * width, autopilot_durationdata, width,
                color='SkyBlue', label='Autopilot')
rects2 = ax.bar(ind- 0.5 * width, pacman_durationdata, width,
                color='Red', label='Pacman')
rects3 = ax.bar(ind + 0.5 * width, supermario_durationdata, width,
                color='Green', label='SuperMario')
rects4 = ax.bar(ind + 1.5 * width, daimler_durationdata, width,
                color='Orange', label='DaimlerModel')

# Add some text for labels, title and custom x-axis tick labels, etc.
ax.set_ylabel('Duration')
ax.set_title('Clusterduration in ms', y=-0.13)
ax.set_xticks(ind)
ax.set_xticklabels(('Spectral','Spectral','Spectral','Markov','Affinity'))
ax.legend(bbox_to_anchor=(0., 1.02, 1., .102), loc=3,
       ncol=2, mode="expand", borderaxespad=0.)

autolabel(rects1, "center")
autolabel(rects2, "center")
autolabel(rects3, "center")
autolabel(rects4, "center")

plt.yscale("log")
plt.show()

#-----------------------------------------------------------------------------------
ind = np.arange(len(cluster_dataaffinity))  # the x locations for the groups
width = 0.15  # the width of the bars

fig, ax = plt.subplots()
rects1 = ax.bar(ind - 2.5 * width, cluster_dataspectral1, width,
                color='SkyBlue', label='Spectral1')
rects2 = ax.bar(ind - 1.5 * width, cluster_dataspectral2, width,
                color='Red', label='Spectral2')
rects3 = ax.bar(ind - 0.5 * width, cluster_dataspectral3, width,
                color='Green', label='Spectral3')
rects4 = ax.bar(ind + 0.5 * width, cluster_datamarkov, width,
                color='Blue', label='Markov')
rects5 = ax.bar(ind + 1.5 * width, cluster_dataaffinity, width,
                color='Orange', label='Affinity')
rects6 = ax.bar(ind + 2.5 * width, componentNumbers, width,
				color='Grey', label='ComponentNumber')

# Add some text for labels, title and custom x-axis tick labels, etc.
ax.set_ylabel('Number of Clusters')
ax.set_title('Number of Clusters')
ax.set_xticks(ind)
ax.set_xticklabels(('Autopilot','Pacman','Supermario', 'DaimlerModel'))
ax.legend(loc='best')

autolabel(rects1, "center")
autolabel(rects2, "center")
autolabel(rects3, "center")
autolabel(rects4, "center")
autolabel(rects5, "center")
autolabel(rects6, "center")

plt.yscale("log")
plt.show()

#-----------------------------------------------------------------------------------
ind = np.arange(len(score_dataaffinity))  # the x locations for the groups
width = 0.15  # the width of the bars

fig, ax = plt.subplots()
rects1 = ax.bar(ind - 2 * width, score_dataspectral1, width,
                color='SkyBlue', label='Spectral1')
rects2 = ax.bar(ind - width, score_dataspectral2, width,
                color='Red', label='Spectral2')
rects3 = ax.bar(ind, score_dataspectral3, width,
                color='Green', label='Spectral3')
rects4 = ax.bar(ind + width, score_datamarkov, width,
                color='Blue', label='Markov')
rects5 = ax.bar(ind + 2 * width, score_dataaffinity, width,
                color='Orange', label='Affinity')

# Add some text for labels, title and custom x-axis tick labels, etc.
ax.set_ylabel('Score')
ax.set_title('Score after Heuristic')
ax.set_xticks(ind)
ax.set_xticklabels(('Autopilot','Pacman','Supermario', 'DaimlerModel'))
ax.legend(loc='best')

autolabel(rects1, "center")
autolabel(rects2, "center")
autolabel(rects3, "center")
autolabel(rects4, "center")
autolabel(rects5, "center")

plt.yscale("log")
plt.show()

#-------------------------------------------------------------------------------
ind = np.arange(len(cluster_dataaffinity))  # the x locations for the groups
width = 0.15  # the width of the bars

fig, ax = plt.subplots()
rects1 = ax.bar(ind - 2 * width, duration_dataspectral1, width,
                color='SkyBlue', label='Spectral1')
rects2 = ax.bar(ind - width, duration_dataspectral2, width,
                color='Red', label='Spectral2')
rects3 = ax.bar(ind, duration_dataspectral3, width,
                color='Green', label='Spectral3')
rects4 = ax.bar(ind + width, duration_datamarkov, width,
                color='Blue', label='Markov')
rects5 = ax.bar(ind + 2 * width, duration_dataaffinity, width,
                color='Orange', label='Affinity')

# Add some text for labels, title and custom x-axis tick labels, etc.
ax.set_ylabel('Duration')
ax.set_title('Clusterduration in ms')
ax.set_xticks(ind)
ax.set_xticklabels(('Autopilot','Pacman','Supermario', 'DaimlerModel'))
ax.legend(loc='best')

autolabel(rects1, "center")
autolabel(rects2, "center")
autolabel(rects3, "center")
autolabel(rects4, "center")
autolabel(rects5, "center")

plt.yscale("log")
plt.show()

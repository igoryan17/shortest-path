import csv
import sys
from statistics import mean, pstdev

import matplotlib.pyplot as plt

DEVIATION_COUNT_TO_FILTER = 4
DIJKSTA_SPARSE_GRAPH = 'время работы алгоритма Дейкстры для разреженного графа'
DIJKSTA_DENSE_GRAPH = 'время работы алгоритма Дейкстры для плотного графа'
DYNAMIC_SPARSE_GRAPH = 'время работы динамического алгоритма для разреженного графа'
DYNAMIC_DENSE_GRAPH = 'вермя работы динамического алгоритма для плотного графа'

vertex_count_to_times = dict()
with open(sys.argv[1], 'r') as csvfile:
    plots = csv.reader(csvfile)
    for row in plots:
        vertex_count_to_times.setdefault(int(row[0]), []).append(int(row[2]))
x = []
y = []
e = []
for vertex_count, times in vertex_count_to_times.items():
    average = mean(times)
    standard_deviation = pstdev(times)
    print(str(vertex_count) + " avg=" + str(average) + " pstdev=" + str(standard_deviation))
    new_time = []
    for time in times:
        if standard_deviation == 0 or \
                abs(time - average) < DEVIATION_COUNT_TO_FILTER * standard_deviation:
            new_time.append(time)
    average = mean(new_time)
    standard_deviation = pstdev(new_time)
    print(str(vertex_count) + " new_avg=" + str(average) + " new_pstdev=" + str(standard_deviation))
    x.append(vertex_count)
    y.append(average)
    e.append(standard_deviation)

plt.errorbar(x, y, yerr=e)
plt.suptitle(DYNAMIC_DENSE_GRAPH)
plt.xlabel('v, количество вершин')
plt.ylabel('t, ms')
plt.show()

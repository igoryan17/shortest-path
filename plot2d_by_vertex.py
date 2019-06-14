import csv
import sys

import matplotlib.pyplot as plt

x = []
y = []
with open(sys.argv[1], 'r') as csvfile:
    plots = csv.reader(csvfile)
    for row in plots:
        x.append(int(row[0]))
        y.append(int(row[2]))
plt.plot(x, y, label='update time depend on edge count')
plt.xlabel('vertexes')
plt.ylabel('time, ms')
plt.show()
import numpy as np
import matplotlib as mpl
import matplotlib.pyplot as plt
import math

def roundWithUncertainties(h, uh, n=2):
	precision = -int(math.floor(math.log10(uh))) + (n-1)
	uh = round(uh, precision)
	h = round(h, precision)
	return (h,uh)

def readFile(filename):
	numbers = []
	for line in open(filename, "r").readlines():
		numbers.append(int(line))
	return np.asarray(numbers)

def getAverageAndDeviation(numarray):
	mean = np.mean(numarray)
	uncertainty = 2*np.std(numarray)/(len(numarray))**0.5

	mean, uncertainty = roundWithUncertainties(mean, uncertainty)

	return mean, uncertainty, len(numarray)#round((uncertainty/mean)*100,2)

def boxPlot(fourway, eightway, track):
	mpl.rcParams.update({'font.size': 18})
	colors = ['lightblue', 'lightgreen', 'pink']
	fig, (ax1, ax2, ax3) = plt.subplots(1,3, figsize=(16,9))
	bps = []

	bps.append(ax1.boxplot(fourway, showfliers=False, patch_artist=True))
	bps.append(ax2.boxplot(eightway, showfliers=False, patch_artist=True))
	bps.append(ax3.boxplot(track, showfliers=False, patch_artist=True))

	for bp in bps:
		for patch, color in zip(bp['boxes'],colors):
			patch.set_facecolor(color)

	for axis, title in zip([ax1, ax2, ax3], ["Fourway", "Eightway", "Combined Track"]):
		axis.set(xticklabels=[], ylabel="Number of crashes in 100000 simulation ticks", title=title)
		axis.tick_params(bottom=False)

	ax3.plot([], linewidth=10, label="NEAT", color='lightblue')
	ax3.plot([], linewidth=10, label="Generalised NEAT", color='lightgreen')
	ax3.plot([], linewidth=10, label="CoSyNE", color='pink')
	fig.tight_layout(pad=2.0)

	ax3.legend(loc="upper right")

	plt.show()

fourway = [readFile("../Data/4way/NEAT.txt"), readFile("../Data/4way/GenNEAT.txt"), readFile("../Data/4way/CoSyNE.txt")]
eightway = [readFile("../Data/8way/NEAT.txt"), readFile("../Data/8way/GenNEAT.txt"), readFile("../Data/8way/CoSyNE.txt")]
track = [readFile("../Data/Track/NEAT.txt"), readFile("../Data/Track/GenNEAT.txt"), readFile("../Data/Track/CoSyNE.txt")]
boxPlot(fourway, eightway, track)

def showBasics():
	print("Track:")
	print("NEAT")
	print(getAverageAndDeviation(readFile("../Data/Track/NEAT.txt")))
	print("GenNEAT")
	print(getAverageAndDeviation(readFile("../Data/Track/GenNEAT.txt")))
	print("CoSyNE")
	print(getAverageAndDeviation(readFile("../Data/Track/CoSyNE.txt")))
	print()
	print("Fourway:")
	print("NEAT")
	print(getAverageAndDeviation(readFile("../Data/4way/NEAT.txt")))
	print("GenNEAT")
	print(getAverageAndDeviation(readFile("../Data/4way/GenNEAT.txt")))
	print("CoSyNE")
	print(getAverageAndDeviation(readFile("../Data/4way/CoSyNE.txt")))
	print()
	print("Eightway:")
	print("NEAT")
	print(getAverageAndDeviation(readFile("../Data/8way/NEAT.txt")))
	print("GenNEAT")
	print(getAverageAndDeviation(readFile("../Data/8way/GenNEAT.txt")))
	print("CoSyNE")
	print(getAverageAndDeviation(readFile("../Data/8way/CoSyNE.txt")))
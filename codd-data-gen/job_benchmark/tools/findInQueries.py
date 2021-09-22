import os

arr = []

def findin(filename):
	infile = open(filename)
	line = infile.readline()
	infile.close()
	# index = line.find("cn_name")
	index = line.find("cct2")
	if index > -1:
		arr.append(filename + " " + line[index - 30: index + 30])

for filename in os.listdir("./"):
	if filename.endswith(".sql"):
		findin(filename)

arr.sort()
for entry in arr:
	print entry
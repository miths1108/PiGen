import os

result = []

def findin(filename):
	infile = open(filename)
	line = infile.readline()
	infile.close()
	index = line.find("cct1.id = cc.subject_id AND cct2.id = cc.status_id")
	if index > -1:
		result.append(filename + " " +line[index - 30: index + 30])

	

for filename in os.listdir("./"):
	if filename.endswith(".sql"):
		findin(filename)

result.sort()
for entry in result:
	print entry
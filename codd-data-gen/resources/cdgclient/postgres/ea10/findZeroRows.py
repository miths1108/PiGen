import os

arr = []

for filename in os.listdir("./"):
	if filename.endswith(".json"):
		infile = open(filename)
		for line in infile:
			if line.find("\"Actual Rows\": 0") > -1:
				arr.append(filename)
				break

arr.sort()

for x in arr:
	print x
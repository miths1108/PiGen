import os

writefile = open("index", 'w')

arr = []

for filename in os.listdir("./"):
	if filename.endswith(".json"):
		arr.append(filename+"\n")

arr.sort()

for x in arr:
	print x,
	writefile.write(x)

writefile.close()
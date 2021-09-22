import os

minn = 50
maxx = 0
summ = 0.0
count = 0

for filename in os.listdir("./"):
	if filename.endswith(".sql"):
		count += 1
		infile = open(filename)
		line = infile.readline()
		infile.close()
		fromList = line[line.upper().find("FROM") + 4:line.upper().find("WHERE")-1].split(',')
		temp = len(fromList)
		if temp < minn:
			minn = temp
		if temp > maxx:
			maxx = temp
		summ += temp

print minn
print summ / count
print maxx
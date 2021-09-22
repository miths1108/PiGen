import os

MAXqueries = 1000

arr = []

for filename in os.listdir("./"):
	if filename.endswith(".sql"):
		arr.append(filename+"\n")

arr.sort()
writefilesql = open("index", 'w')
writefilejson = open("../ea/index", 'w')

i = 1
for x in arr:
	print x[0:len(x)-5]
	writefilesql.write(x)
	writefilejson.write(x[0:len(x)-4] + "json\n")
	if i >= MAXqueries:
		break
	i += 1

writefilesql.close()
writefilejson.close
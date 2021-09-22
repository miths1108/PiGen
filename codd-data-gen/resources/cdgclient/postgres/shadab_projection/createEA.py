import os
from multiprocessing.dummy import Pool as ThreadPool 
pool = ThreadPool(3)

explainOnly = True
explainOnly = False

count = 200
# count = 0

arr = []
cmdarray = []

def execu((cmd,filename)):
	os.system(cmd)
	print(filename+".json")


def create(filename):
	infile = open(filename)
	lines = "\n".join(infile.readlines())
	infile.close()
	filename = filename[0:len(filename) - 4]
	if explainOnly:
		cmd = "/Library/PostgreSQL/9.5/bin/psql -t -d tpcds10 -A -U postgres -o \"../ea10/" + filename + ".json\" -c \"EXPLAIN (FORMAT JSON)" + lines + "\""
	else:
		cmd = "/Library/PostgreSQL/9.5/bin/psql -t -d tpcds10 -A -U postgres -o \"../ea10/" + filename + ".json\" -c \"EXPLAIN (ANALYSE, FORMAT JSON)" + lines + "\""
	cmdarray.append((cmd,filename))
	# print(cmd)

if count > 0:
	for filename in os.listdir("./"):
		if filename.endswith(".sql"):
			arr.append(filename)

	arr.sort()
	for x in arr:
		create(x)
		count -= 1
		if count == 0:
			break

else:
	create("q004.sql")

results = pool.map(execu, cmdarray)
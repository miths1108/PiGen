import os
from multiprocessing.dummy import Pool as ThreadPool 
pool = ThreadPool(12)

count = 0

arr = []
cmdarray = []

def execu(cmd):
	os.system(cmd)
	print cmd[33:43]


def create(filename):
	infile = open(filename)
	line = infile.readline()
	infile.close()
	filename = filename[0:len(filename) - 4]
	cmd = "psql -t -d imdbload -A -o \"../ea/" + filename + ".json\" -c \"EXPLAIN (ANALYSE, FORMAT JSON)" + line[0:len(line) - 1] + "\""
	cmdarray.append(cmd)

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
	# create("23a_3.sql")
	create("29b_5.sql")
	# create("7c_3.sql")

results = pool.map(execu, cmdarray)
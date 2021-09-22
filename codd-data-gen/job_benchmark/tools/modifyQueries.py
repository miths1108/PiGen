import os

mapperTableNameTOInitial = {'complete_cast': 'cc', 'person_info': 'pi', 'movie_keyword': 'mk', 'keyword': 'k', 'aka_title': 'at', 'movie_info_idx': 'mii', 'title': 't', 'company_type': 'ct', 'cast_info': 'ci', 'comp_cast_type': 'cct', 'name': 'n', 'info_type': 'it', 'role_type': 'rt', 'company_name': 'cn1', 'movie_info': 'mi', 'aka_name': 'an', 'movie_companies': 'mc', 'kind_type': 'kt', 'movie_link': 'ml', 'link_type': 'lt', 'char_name': 'cn'}
mapperAsToInitial = {}

def edit(filename):
	# print filename
	infile = open(filename)
	line = infile.readline()
	infile.close()
	
	# arr = line.split(" ")
	# i = 0
	# while arr[i] != "FROM":
	# 	i += 1
	# i += 1
	# while arr[i] != "WHERE":
	# 	if arr[i+1] == "AS":
	# 		asinitial = arr[i+2]
	# 		if asinitial[len(asinitial) - 1] == ',':
	# 			asinitial = asinitial[0:len(asinitial) - 1]
	# 		asinitial += "."
	# 		tablename = arr[i]
	# 		mapperAsToInitial[asinitial] = mapperTableNameTOInitial[tablename]
	# 		i += 3
	# 	else:
	# 		asinitial = arr[i]
	# 		if asinitial[len(asinitial) - 1] == ',':
	# 			asinitial = asinitial[0:len(asinitial) - 1]
	# 		asinitial += "."
	# 		i += 1

	# i = 0
	# fromIndex = line.find("FROM")
	# line = "SELECT * " + line[fromIndex: len(line)]
		

	# for key in mapperAsToInitial:
	# 		value = mapperAsToInitial[key]
	# 		line = line.replace("("+ key, "(" + key + value + "_" ).replace(" "+ key, " " + key + value + "_" )

	# writefile = open(filename, 'w')
	# writefile.write(line)
	# writefile.close()

for filename in os.listdir("./"):
	if filename.endswith(".sql"):
		edit(filename)

# edit("13a.sql")
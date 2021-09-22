tables_set = [["mi_idx1","t1","it1","kt1"], ["ml","t1","t2","kt1","lt"], ["mc1","t1","cn1","kt1"], ["mc2","t2","cn2","kt2"], ["mi_idx2","t2","it2","kt2"]] #,["an","n"],["pi","n","it3"]] # , ["cc","t","cct1","cct2"] ,["mi","it1","t"], ["ci","t","n"],
files = ["33a.sql", "33b.sql", "33c.sql"]#, "33d.sql"]

def stripall(arr):
	newarr = []
	for entry in arr:
		newarr.append(entry.strip())
	return newarr

alltables = set()
for tables in tables_set:
	for table in tables:
		alltables.add(table)

print alltables

finalList= []

for file in files:
	fileobj = open(file)
	query = fileobj.readline()
	fileobj.close()
	if query.find("and") > -1:
		raise Exception("query contain lower AND : " + file)
	if query.lower().find(" or ") > -1:
		raise Exception("query contain OR : " + file)
	if query.find("!=") > -1:
		print "query contain != : " + file
	if query.lower().find("null") > -1:
		raise Exception("query contain null " + file)
	if query.lower().count("from") > 1 or query.lower().count("where") > 1:
		raise Exception("query contain more than 1 from or where " + file)

	filePrefix = file.split('.')[0]
	fileindex = 1
	for tables in tables_set:
		newQuery = "SELECT * FROM"
		qFROM = query[query.upper().find("FROM") + 4:query.upper().find("WHERE")-1].split(',')
		qFROM = stripall(qFROM)
		print "---------------------------------------"
		accepted = []
		for entry in qFROM:
			if entry.split(" ")[2] not in alltables:
				raise Exception("Table in from not found in alltables",entry,file)
			if entry.split(" ")[2] in tables:
				# print entry.split(" ")
				accepted.append(entry)
		for entry in accepted:
			newQuery += " " + entry + ","
		newQuery = newQuery[0:len(newQuery)-1] + " WHERE "

		qWHERE = query[query.upper().find("WHERE") + 5:len(query) - 2].split('AND')
		qWHERE = stripall(qWHERE)
		# print qWHERE

		accepted = []
		for entry in qWHERE:
			entryArr = entry.split('.')
			if len(entryArr) == 2:
				t_name = entryArr[0]
				# print "2",t_name
				if not t_name in alltables:
					raise Exception("Table not found in alltables : " + t_name)
				if t_name in tables:
					if entry.lower().find("between") > -1:
						print "FOUND between. Entry : " + entry + " in query " + file
					if entry.lower().find("like") > -1:
						print "Discarding like : " + entry
						continue
					accepted.append(entry)
					
			elif len(entryArr) == 3:
				# print entry,file
				temp = entryArr[1].split("=")[1]
				t_name1 = entryArr[0]
				t_name2 = temp.strip()
				# print "3",t_name1, t_name2
				if t_name1 not in alltables or t_name2 not in alltables:
					raise Exception("One table not found in alltables : " + t_name1 + " " + t_name2)
				if t_name1 in tables and t_name2 in tables:
					accepted.append(entry)
			else:
				raise Exception("More than 3 .'s found in : " + entry, file)
			# print len(entry.split('.')), entry

		for entry in accepted:
			newQuery += entry + " AND "
		newQuery = newQuery[0:len(newQuery)-4].strip() + ";\n"
		finalList.append((filePrefix + "_" + str(fileindex) + ".sql", newQuery))
		fileindex += 1

for ((filename, newQuery)) in finalList:
	# print filename, newQuery
	writeObj = open(filename, 'w')
	writeObj.write(newQuery)
	writeObj.close()

# Handle AND of between
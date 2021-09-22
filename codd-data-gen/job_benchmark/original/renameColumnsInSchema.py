import os

mapperInitialToTableName = {}
mapperTableNameTOInitial = {}

file = open("schema.sql")
curTable = ""
for line in file:
	if line.startswith("CREATE TABLE "):
		arr = line.split(" ")
		tablename = arr[2]
		curTable = tablename
		tablenameArr = tablename.split("_")
		tablenameInitials = ""
		for part in tablenameArr:
			tablenameInitials += part[0]
		while True:
			if tablenameInitials in mapperInitialToTableName:
				tablenameInitials = tablenameInitials+str(1)
			else:
				mapperInitialToTableName[tablenameInitials] = tablename
				mapperTableNameTOInitial[tablename] = tablenameInitials
				break
	elif line.startswith("    "):
		arr = line.split(" ")
		print "alter table " + curTable + " rename " + arr[4] + " TO " + mapperTableNameTOInitial[curTable] + "_" + arr[4] + ";"

# print mapperInitialToTableName
print mapperTableNameTOInitial
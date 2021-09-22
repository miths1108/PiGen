import os

allfiles = os.listdir("./")

# allfiles = ["query15_2.sql","query14_1_8_3.sql","query14_1_2.sql","query4_2_4.sql","query4_2_8.sql","query4_2_6.sql","query14_1_8_2.sql","query4_2_7.sql","query4_2_2.sql","query4_2_1.sql","query15_1.sql","query14_1_5.sql","query4_2_3.sql","query10_4.sql","query14_1_8_1.sql","query2_2.sql","query4_2_5.sql","query5_2_1.sql","query16.sql","query18_3.sql","query18_4.sql","query18_2.sql","query18_1.sql","query20_4.sql","query20_2.sql","query20_3.sql","query20_5.sql","query20_1.sql"]

arr = []
for filename in allfiles:
	if filename.endswith(".sql"):
		arr.append(filename)

def stripColName(colName):
	temp = colName.split(".")
	return temp[len(temp) - 1]

projCols = set()
for filename in arr:
	infile = open(filename)
	lines = "\n".join(infile.readlines())
	lines = lines.lower()
	infile.close()
	startIndex = lines.find("group by ")
	if startIndex != -1:
		startIndex += 9
		endIndex = lines.find(";", startIndex);
		projCols.add(stripColName(lines[startIndex:endIndex]))
	startIndex = lines.find("distinct")
	if startIndex != -1:
		startIndex = lines.find("(", startIndex) + 1;
		endIndex = lines.find(")", startIndex);
		projCols.add(stripColName(lines[startIndex:endIndex]))

	startIndex = lines.find("from")
	startIndex += 5
	endIndex = lines.find("where")
	if endIndex == -1:
		raise "no where"
	fromClause = lines[startIndex: endIndex]
	for table in fromClause.split(","):
		table = table.strip()
		temp = table.split(" ")
		table = temp[0]
		table = table.strip()
		if table == "store_sales":
			print (filename[:len(filename)-3] + 'json')
			break
			
print("number of projected columns: " + str(len(projCols)))
# print(projCols)
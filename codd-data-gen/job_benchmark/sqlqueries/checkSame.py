import os

arr = []

for filename in os.listdir("./"):
	if filename.endswith(".sql"):
		arr.append(filename)

sets = set()

count = 0
for filename in arr:
	content = open(filename).read()
	if content in sets:
		print filename
		os.rename(filename, 'redundant/'+filename)
		os.rename("ea/" + filename.split('.')[0] + ".json", 'ea/redundant/'+filename.split('.')[0] + ".json")
		count += 1
	else:
		sets.add(content)

print count
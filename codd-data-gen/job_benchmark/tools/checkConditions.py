arr = []

while True:
	temp = raw_input()
	if temp == "e":
		break
	else:
		arr.append(temp)

sets = {}
for entry in arr:
	temp = entry.split('=')
	if temp[0] in sets:
		if temp[1] != sets[temp[0]]:
			print temp
			break
	else:
		sets[temp[0]] = temp[1]
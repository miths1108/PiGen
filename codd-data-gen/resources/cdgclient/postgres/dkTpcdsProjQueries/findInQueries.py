import os

arr = []

def findin(filename):
    infile = open(filename)
    lines = " ".join(infile.readlines())
    infile.close()
    distinctIndex = lines.find("d_year")
    index = lines.find("d_", distinctIndex + 13)
    if index > -1:
        arr.append(filename + " " + lines[index - 30: index + 30])

for filename in os.listdir("./"):
    if filename.endswith(".sql"):
        findin(filename)

arr.sort()
for entry in arr:
    print(entry)

print("end")
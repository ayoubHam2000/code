folderList = ['android', 'General', 'Learning', 'Unity']
dis = 'readme.md'

outputfile = open(dis, 'w')
for item in folderList:
    f_item = open(f'./{item}/readme.md', 'r')
    d_item = f_item.read()
    outputfile.write(d_item)
    outputfile.write("\n***\n")
    f_item.close()
outputfile.close()
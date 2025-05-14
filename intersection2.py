import sys

f = open(sys.argv[1], 'r')
g = open(sys.argv[2], 'r')
data1 = f.readlines()
data2 = g.readlines()

exact1 = set()
relaxed1 = set()
exact2 = set()
relaxed2 = set()
for a in data1:
    b = a.replace('\n','').split(',')
    exact1.add(','.join([i for sub in sorted(((b[0],b[1],b[2],b[3]),(b[4],b[5],b[6],b[7]))) for i in sub]))
    relaxed1.add(','.join([i for sub in sorted(((b[0],b[1],b[3]),(b[4],b[5],b[7]))) for i in sub]))
for a in data2:
    b = a.replace('\n','').split(',')
    exact2.add(','.join([i for sub in sorted(((b[0],b[1],b[2],b[3]),(b[4],b[5],b[6],b[7]))) for i in sub]))
    relaxed2.add(','.join([i for sub in sorted(((b[0],b[1],b[3]),(b[4],b[5],b[7]))) for i in sub]))

# intersection = relaxed1.intersection(relaxed2)
intersection = set()
for a in exact1:
     b = a.split(',')
     c = ','.join([i for sub in sorted(((b[0],b[1],b[3]),(b[4],b[5],b[7]))) for i in sub])
     if (c in relaxed2):
         intersection.add(a)
f = open(sys.argv[1]+'_'+sys.argv[2]+'_intersection.csv', 'w')
f.write('\n'.join(intersection))
f.close()

# nicad = relaxed1.difference(relaxed2)
nicad = set()
for a in exact1:
     b = a.split(',')
     c = ','.join([i for sub in sorted(((b[0],b[1],b[3]),(b[4],b[5],b[7]))) for i in sub])
     if (c not in relaxed2):
         nicad.add(a)
f = open(sys.argv[1]+'_exclusive.csv', 'w')
f.write('\n'.join(nicad))
f.close()

# ash = relaxed2.difference(relaxed1)
ash = set()
for a in exact2:
     b = a.split(',')
     c = ','.join([i for sub in sorted(((b[0],b[1],b[3]),(b[4],b[5],b[7]))) for i in sub])
     if (c not in relaxed1):
         ash.add(a)
f = open(sys.argv[2]+'_exclusive.csv', 'w')
f.write('\n'.join(ash))
f.close()

print("Number of clone pairs for %s: %d" % (sys.argv[1],(len(data1))))
print("Number of clone pairs for %s: %d" % (sys.argv[2],(len(data2))))
print("Number of unique clone pairs for %s: %d" % (sys.argv[1],(len(relaxed1))))
print("Number of unique clone pairs for %s: %d" % (sys.argv[2],(len(relaxed2))))
print("Number of common clone pairs: %d" % (len(intersection)))
print("Number of exclusive clone pairs for %s: %d" % (sys.argv[1],(len(nicad))))
print("Number of exclusive clone pairs for %s: %d" % (sys.argv[2],(len(ash))))

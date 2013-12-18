import ketama
import sys


connections = {}
filename = ''
if len(sys.argv) < 2:
   print "Usage: test_python.py key_to_be_tested"
   sys.exit()
elif len(sys.argv) == 3:
    filename = sys.argv[2]
    print 'Testing file: ' + filename
    cont = ketama.Continuum(filename)    
else:
    cont = ketama.Continuum('key:12324')

test_key  = sys.argv[1]

print "Testing key: " + test_key
servers = open('../ketama.servers')
for server in servers:
   server_info = server.split()
   server_name = server_info[0]
   memory = int(server_info[1])
   cont.add_server(server_name, memory)
   print "Adding server: " + server_name + ":" + str(memory)

info = cont.get_info()
print info
cont.sync_servers("node1:1000,node2:1000,node3:1000,node4:1000")
print cont.get_info()

output = open('../python_test.out', 'w')
for num in range(0, 100):
    server = cont.get_server(test_key + str(num))
    output.write(server[1] + ' - ' + test_key + str(num) + '\n');

output.close()
print 'Output written'
cont.destroy()

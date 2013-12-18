/*
 * Using a known ketama.servers file, and a fixed set of keys
 * print and hash the output of this program using your modified
 * libketama, compare the hash of the output to the known correct
 * hash in the test harness.
 *
 */

#include "ketama.h"
#include <stdio.h>
#include <stdlib.h>

int main(int argc, char **argv)
{
  if(argc != 3){
	printf("Usage: %s <ketama.servers file> <key_prefix>\n", *argv);
	return 1;
  }

  ketama_continuum c;
  ketama_roll( &c, *++argv );

  char key_prefix[10];
  snprintf(key_prefix, sizeof(key_prefix), "%s", argv[1]);

  printf( "Ketama info: %s\n", ketama_info(c) );
  printf( "%s\n", ketama_error() );

  int hash = ketama_hashi("test");
  printf("Ketama hash: %i\n", hash);

  char node_names[1000];
  int rand1, i = 0;
  srand(time(NULL));
  printf( "Ketama info: %s\n", ketama_info(c) );
  for(i; i < 10; i++) {
        rand1 = rand();
        char node_id1[10];
        sprintf(node_id1, "%d", rand1);
        rand1 = rand();
        char node_id2[10];
        sprintf(node_id2, "%d", rand1);
        sprintf(node_names, "node%s:1000,node%s:1000,node3:1000", node_id1, node_id2);
        printf("Ketama prior sync: %s\n", ketama_info(c) );
        sync_servers(node_names, c);
    }
  sprintf(node_names, "%s", "node1:1000,node2:1000,node3:1000,node4:1000");
  sync_servers(node_names, c);
  printf("Ketama info: %s\n", ketama_info(c) );

  ketama_print_continuum(c);

  FILE * pFile;
  pFile = fopen ("../c_test.out","w");
  if (pFile!=NULL)
  {
    for ( i = 0; i < 100; i++ )
    {
        char k[10];
        char output[20];
        //unsigned int kh = ketama_hashi( k );
        snprintf(k, sizeof(k), "%s%i", key_prefix, i);
        mcs* m = ketama_get_server(k, c );
        sprintf(output, "%s - %s\n", m->ip, k );
        fputs(output, pFile);
    }
    fclose (pFile);
  }
  printf("output file saved\n");
  ketama_smoke(c);

  return 0;
}

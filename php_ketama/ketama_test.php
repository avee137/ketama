#!/usr/bin/php
<?php

  // The Ketama ring
    $ketama_ring      = null;

    $cluster_nodes      = array();
    $mem_key          = hash('crc32', 'sample_key');
    print "Key for ketama: " . $mem_key . "\n";
    $server_list = '../ketama.servers';
    if ( count($argv) < 2) {
      print "Provide key prefix\n";
      //exit;
    } elseif (count($argv) == 3) {
      $server_list = $argv[2];
    }
    $key = $argv[1];


  /**
   * Loads the list of nodes and initializes the Ketama ring for it
   *
   * @return void
   */
   function init() {
    global $ketama_ring, $cluster_nodes, $mem_key, $server_list;
    $cluster_nodes = array();
    if (empty($cluster_nodes)) {
      $file = fopen($server_list, 'r');
      if ($file) {
        $line = fgets($file);
        while($line) {
	   $values = explode(' ', $line);
	   $name = $values[0];
       $ram = $values[1];
	   $redis_nodes[$name]['name'] = $name;
	   $redis_nodes[$name]['mem'] = $ram;
	   $line = fgets($file);
        }
        fclose($file);
      } else {
         print "No server file found: " . $server_list . "\n";
         exit;
      }
    } else {
      return;
    }

    // Initialize the Ketama ring
    $ketama_ring = ketama_roll('key:' . $mem_key);
    if(!empty($ketama_ring)){
      print "Ketama ring populated\n";
    }
    foreach ($cluster_nodes as $name => $info) {
      print "Adding server: " . $name . " with mem: " . $info['mem'] . "\n";
      ketama_add_server($name, (int)$info['mem'], $ketama_ring);
      print ketama_error();
    }
    print "Done adding servers\n";
  }



  init();
  print "Getting server info:\n";
  print ketama_get_info($ketama_ring) . "\n";
  for ($i=0; $i < 10; $i++) {
    $rand1 = rand();
    $rand2 = rand();
    $result = ketama_sync_servers("redis1:1000,redis2:1000,redis" .
        $rand1 .":1000,redis" . $rand2 . ":1000", $ketama_ring);
    print ketama_error();
    print ketama_get_info($ketama_ring) . "\n\n\n";
  }
  $result = ketama_sync_servers("node1:1000,node2:1000,node3:1000,node4:1000", $ketama_ring);
  print ketama_get_info($ketama_ring) . "\n";
  //Print an output file of 100 keys and their assigned nodes
  //We can use this to verify that the same file is produces by other
  //implementations of ketama
  $output = fopen('../php_test.out', 'w+');
  for ($i = 0; $i < 100; $i++) {
    $server = ketama_get_server($key . $i, $ketama_ring);
    if(empty($server)) {
       print "No server for key: " . $key . $i . "\n";
    } else {
      $name = $server['name'];
      fwrite($output, $name . ' - ' . $key . $i . "\n");
    }
  }
  fclose($output);
  print "Output written\n";
  ketama_destroy($ketama_ring);

?>

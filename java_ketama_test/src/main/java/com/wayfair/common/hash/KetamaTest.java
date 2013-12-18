package com.wayfair.common.hash;

import com.wayfair.common.hash.ketama.FNV1A32HashFunction;
import com.wayfair.common.hash.ketama.FNV1AServerHashStrategy;
import com.wayfair.common.hash.ketama.KetamaRing;
import com.wayfair.common.hash.ketama.KetamaServer;

import java.io.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.String;
import java.util.Set;
import java.util.HashSet;

public class KetamaTest {

    public static void main(String[] args) throws IOException {
        String serverListPath = args[0];
        String outputPath = args[1];

        Set<KetamaServer> servers = new HashSet<KetamaServer>();
        BufferedReader serverListReader = new BufferedReader(new FileReader(new File(serverListPath)));
        String line;
        while ((line = serverListReader.readLine()) != null) {
            String[] parts = line.split(" ");
            servers.add(new KetamaServer(parts[0], parts[0], 1234));
        }

        KetamaRing ring = new KetamaRing(new FNV1AServerHashStrategy(), new FNV1A32HashFunction(), servers);
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outputPath, false)));
        for (int i = 0 ; i < 100 ; i++) {
            String key = "aab" + i;
            KetamaServer server = ring.get(key);
            out.println(server.getName() + " - " + key);
        }
        out.close();
    }
}
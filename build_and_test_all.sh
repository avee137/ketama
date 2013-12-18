#!/bin/bash

export LD_LIBRARY_PATH=/usr/local/lib64:$LD_LIBRARY_PATH
#Clean up test files
rm c_test.out php_test.out python_test.out java_test.out erlang_test.out lua_test.out

#Build and test C
cd libketama
./configure --with-fnv
make clean
make
sudo make install
./ketama_test ../ketama.servers aab

#Build and test python
cd ../python_ketama
python setup.py bdist_egg
sudo easy_install dist/ketama-0.1.8-py2.7-linux-x86_64.egg
python test_ketama.py aab

#Build and test php
cd ../php_ketama
phpize
./configure
make clean
make
sudo make install
php ketama_test.php aab

#Build and test lua
cd ../lua_ketama
make clean
make
lua test.lua

#Build and test java
cd ../java_ketama
mvn clean
mvn install
cd ../java_ketama_test
mvn clean
mvn package
java -jar target/java_ketama_test-1.0.jar ../ketama.servers ../java_test.out

#Build and test erlang
cd ../erlang
erl -noshell -eval 'ketama:start_link(),
                    ketama:getserver("test"),
                    init:stop().'

#Test the output files
echo "Testing the output files from all languages"
cd ../
diff c_test.out php_test.out
diff c_test.out python_test.out
diff c_test.out lua_test.out
diff c_test.out java_test.out
diff c_test.out erlang_test.out

#Display sha1 hash of file
sha1sum c_test.out
sha1sum php_test.out
sha1sum python_test.out
sha1sum erlang_test.out
sha1sum java_test.out
sha1sum lua_test.out


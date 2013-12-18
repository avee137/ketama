erl -noshell -eval 'ketama:start_link(), 
		    ketama:getserver("test"), 
		    init:stop().'

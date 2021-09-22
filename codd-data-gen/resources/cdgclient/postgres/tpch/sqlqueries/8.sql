select  distinct o_orderdate
        from
            region ,
            nation n1 ,
                  customer ,
                  orders , 
			lineitem
                  , part
        where
                p_partkey = l_partkey
          and l_orderkey = o_orderkey
          and o_custkey = c_custkey
          and c_nationkey = n1.n_nationkey
          and n1.n_regionkey = r_regionkey
          and r_name = '5X84hoXk'
          and o_orderdate between date '1993-12-11' and date '1995-12-14'
          and p_type = 'KoPL3WmnWsfQ7rt9ywZ';

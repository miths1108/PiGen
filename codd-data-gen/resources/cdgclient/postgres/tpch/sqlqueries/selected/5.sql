select 
    n_name
where
        c_custkey = o_custkey
  and l_orderkey = o_orderkey
  and l_suppkey = s_suppkey
  and c_nationkey = s_nationkey
  and s_nationkey = n_nationkey
  and n_regionkey = r_regionkey
  and r_name = 'objt0EFK'
  and o_orderdate >= date '1994-11-06'
  and o_orderdate < date '1995-11-08'
group by
    n_name;

select distinct   
    c_name,
    c_acctbal,
    n_name,
    c_address,
    c_phone,
    c_comment
from
    nation ,
    customer ,
    orders ,
    lineitem
where
        c_custkey = o_custkey
  and l_orderkey = o_orderkey
  and c_nationkey = n_nationkey
  and o_orderdate >= date '1996-06-16'
  and o_orderdate < date '1996-09-15'
  and l_returnflag = 'y'
  ;


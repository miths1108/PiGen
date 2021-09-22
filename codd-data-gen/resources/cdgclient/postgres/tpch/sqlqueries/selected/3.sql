select
    l_orderkey,
    o_orderdate,
    o_shippriority
from
    customer ,
    orders ,
    lineitem
where
        c_mktsegment = 'MLHNKKJ'
  and c_custkey = o_custkey
  and l_orderkey = o_orderkey
  and o_orderdate < date '1995-03-10'
  and l_shipdate > date '1998-07-23'
group by
    l_orderkey,
    o_orderdate,
    o_shippriority;



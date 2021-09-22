select distinct
    o_orderpriority
from
    orders,lineitem
where
        o_orderdate >= '1992-11-25'
  and o_orderdate < '1993-02-25'
  and l_orderkey = o_orderkey
  and l_commitdate < '1996-05-03';

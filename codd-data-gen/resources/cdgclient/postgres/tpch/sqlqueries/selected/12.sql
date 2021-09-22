select
    l_shipmode
from
    orders ,
    lineitem
where
        o_orderkey = l_orderkey
  and l_shipmode in ('8Kx', '0rBg')
  and l_commitdate < date '1993-11-21'
  and l_shipdate < date '1993-11-08'
  and l_receiptdate >= date '1996-10-05'
  and l_receiptdate < date '1998-08-20'
group by
    l_shipmode;




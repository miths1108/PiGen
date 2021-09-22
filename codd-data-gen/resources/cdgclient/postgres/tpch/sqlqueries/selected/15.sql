select
    l_suppkey
from
    lineitem
where
        l_shipdate >= '1997-06-15'
  and l_shipdate < '1997-09-18'
group by
    l_suppkey;

                   

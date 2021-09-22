select distinct
    l_shipmode
from
    lineitem
where
        l_shipdate >= '1997-06-15'
  and l_shipdate < '1997-09-18';

                   

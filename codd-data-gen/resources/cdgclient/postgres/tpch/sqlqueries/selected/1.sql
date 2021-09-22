select
    l_returnflag,
    l_linestatus
from
    lineitem
where
        l_shipdate <= date '1998-10-26'
group by
    l_returnflag,
    l_linestatus;




select distinct
    l_returnflag,
    l_linestatus
from
    lineitem
where
        l_shipdate <= date '1998-10-26';




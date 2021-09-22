-- query 1

select distinct l_returnflag, l_linestatus
from
	lineitem
where
	l_shipdate <= '1998-12-01' and l_shipdate >= '1998-09-01'
;

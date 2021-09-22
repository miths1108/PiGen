-- query 20

select distinct
	l_partkey
	l_suppkey
from
	lineitem
where
	l_shipdate >= '1997-01-01'
	and l_shipdate < '1998-01-01'
;
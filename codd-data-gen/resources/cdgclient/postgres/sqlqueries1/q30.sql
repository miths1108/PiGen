-- query 15

select distinct
	l_suppkey
from
	lineitem
where
	l_shipdate >= '1997-05-01'
	and l_shipdate < '1997-08-01'
;
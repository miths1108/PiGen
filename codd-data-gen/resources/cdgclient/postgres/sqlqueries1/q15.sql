-- query 14

select *	
from
	lineitem,
	part
where
	l_partkey = p_partkey
	and l_shipdate >= '1993-11-01'
	and l_shipdate < '1993-12-01'
;

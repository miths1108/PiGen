-- query 6

select *
from
	lineitem
where
	l_shipdate >= '1995-01-01'
	and l_shipdate < '1996-01-01'
	and l_discount between 0.08 and 0.10
	and l_quantity < 24
;

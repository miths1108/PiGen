-- query 4

select	
	distinct o_orderpriority
from
	orders
where
	o_orderdate >= '1996-03-01'
	and o_orderdate < '1996-06-01'
;

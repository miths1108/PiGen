-- query 3

select distinct l_orderkey, o_orderdate, o_shippriority
from
	customer,
	orders,
	lineitem
where
	c_mktsegment = 'HOUSEHOLD'
	and c_custkey = o_custkey
	and l_orderkey = o_orderkey
	and o_orderdate < '1995-03-21'
	and l_shipdate > '1995-03-21'
;

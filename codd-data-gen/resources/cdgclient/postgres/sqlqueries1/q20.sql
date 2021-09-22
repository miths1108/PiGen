select distinct
	c_name,
	c_custkey,
	o_orderkey,
	o_orderdate,
	o_totalprice
from
	customer,
	orders,
	lineitem
where
	c_custkey = o_custkey
	and o_orderkey = l_orderkey
;

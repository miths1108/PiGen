-- query 8

select distinct 
	o_orderdate
from
	part,
	supplier,
	lineitem,
	orders,
	customer,
	nation,
	region
where
	p_partkey = l_partkey
	and s_suppkey = l_suppkey
	and l_orderkey = o_orderkey
	and o_custkey = c_custkey
	and c_nationkey = n_nationkey
	and n_regionkey = r_regionkey
	and r_name = 'ASIA'
	and s_nationkey = n_nationkey
	and o_orderdate >= '1995-01-01' and o_orderdate <=  '1996-12-31'
	and p_type = 'LARGE BRUSHED NICKEL'
;
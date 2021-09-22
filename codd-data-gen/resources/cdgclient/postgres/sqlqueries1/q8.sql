-- query 7

select distinct
	n_name,	
	l_shipdate
from
	supplier,
	lineitem,
	orders,
	customer,
	nation
where
	s_suppkey = l_suppkey
	and o_orderkey = l_orderkey
	and c_custkey = o_custkey
	and s_nationkey = n_nationkey
	and c_nationkey = n_nationkey
	and n_name = 'RUSSIA' or n_name = 'INDIA'
	and l_shipdate <= '1995-01-01' and l_shipdate >= '1996-12-31'
;

-- query 21

select distinct
	s_name
from
	supplier,
	lineitem,
	orders,
	nation
where
	s_suppkey = l_suppkey
	and o_orderkey = l_orderkey
	and o_orderstatus = 'F'
	-- and l_receiptdate > l_commitdate
	and s_nationkey = n_nationkey
	and n_name = 'VIETNAM'
;
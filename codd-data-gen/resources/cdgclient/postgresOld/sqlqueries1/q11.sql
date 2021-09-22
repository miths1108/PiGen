-- query 10

select distinct 
	c_custkey,
	c_name,
	c_acctbal,
	c_phone,
	n_name,
	c_address,
	c_comment
from
	customer,
	orders,
	lineitem,
	nation
where
	c_custkey = o_custkey
	and l_orderkey = o_orderkey
	and o_orderdate >= '1995-01-01'
	and o_orderdate < '1995-04-01'
	and l_returnflag = 'R'
	and c_nationkey = n_nationkey
;

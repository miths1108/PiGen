-- query 2

select *
from
	partsupp,
	supplier,
	nation,
	region,
	part
where
	p_partkey = ps_partkey
	and s_suppkey = ps_suppkey
	and s_nationkey = n_nationkey
	and n_regionkey = r_regionkey
	and r_name = 'EUROPE'
;

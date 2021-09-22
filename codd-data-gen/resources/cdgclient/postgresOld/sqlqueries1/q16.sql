-- query 16 (to be added)

select distinct
	p_brand,
	p_type,
	p_size
from
	partsupp,
	part
where
	p_partkey = ps_partkey
	and p_brand <> 'Brand#53'
	-- and p_type not like 'LARGE ANODIZED%'
	and p_size in (45, 37, 43, 7, 18, 13, 22, 12)
	-- and ps_suppkey not in ()
;

-- query 17

select *
from
	lineitem,
	part
where
	p_partkey = l_partkey
    and p_brand = 'brand#33'
	and p_container = 'wrap jar'
;

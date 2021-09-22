-- query 20

select *
from
	supplier,
	nation
where
	s_nationkey = n_nationkey
	and n_name = 'ARGENTINA'
;

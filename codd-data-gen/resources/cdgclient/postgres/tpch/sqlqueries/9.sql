 select distinct n_name, o_orderdate
from nation, supplier, partsupp, part, lineitem, orders
where     s_suppkey = l_suppkey
          and ps_suppkey = l_suppkey
          and ps_partkey = l_partkey
          and p_partkey = l_partkey
          and o_orderkey = l_orderkey
          and s_nationkey = n_nationkey
          and p_name ='EBB4nprx90JSfIc8NQYVimx6Ge5pDEt5KSOUqc';


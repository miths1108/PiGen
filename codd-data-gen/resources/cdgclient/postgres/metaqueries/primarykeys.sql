-- https://dba.stackexchange.com/a/28151/134530
select 
	-- tc.table_schema, 
	tc.table_name, kc.column_name 
from  
    information_schema.table_constraints tc,  
    information_schema.key_column_usage kc  
where 
    tc.constraint_type = 'PRIMARY KEY' 
    and kc.table_name = tc.table_name and kc.table_schema = tc.table_schema
    and kc.constraint_name = tc.constraint_name
order by 1, 2;
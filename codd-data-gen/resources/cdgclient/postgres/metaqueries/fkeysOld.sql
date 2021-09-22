SELECT 
    tables_source.table_name AS table_name_source, 
    tables_target.table_name AS table_name_target,
    kcu.column_name AS column_name_source
FROM
     information_schema.table_constraints AS tables_source
INNER JOIN
     information_schema.referential_constraints AS fkeys
ON
    tables_source.constraint_name = fkeys.constraint_name
INNER JOIN
     information_schema.table_constraints  AS tables_target
ON
    fkeys.unique_constraint_name = tables_target.constraint_name
INNER JOIN
	information_schema.key_column_usage AS kcu
ON 
	tables_source.constraint_name = kcu.constraint_name

WHERE tables_source.table_catalog='<postgres-basicschema-dbname>'
    AND tables_source.table_schema='<postgres-basicschema-schemaname>'
ORDER BY
    tables_source.table_name, tables_target.table_name;
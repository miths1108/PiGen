SELECT
    tables.table_name, 
    columns.column_name,
    keys.constraint_name, 
    (keys.constraint_name IS NOT NULL) AS is_key,
    columns.data_type,
    columns.ordinal_position
FROM
    information_schema.tables AS tables
INNER JOIN
    information_schema.columns AS columns
ON
    tables.table_name =  columns.table_name
LEFT OUTER JOIN
    information_schema.key_column_usage AS keys
ON
    columns.column_name = keys.column_name
WHERE tables.table_catalog='<postgres-basicschema-dbname>'
    AND tables.table_schema='<postgres-basicschema-schemaname>'
    AND tables.table_type='BASE TABLE'
ORDER BY
    tables.table_name, is_key DESC, columns.column_name;

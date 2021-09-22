SELECT * FROM
    t12,
    t03,
    t07,
    t06,
    t13
WHERE 1=1 
    AND t03_F_t13 = t13_P
    AND t03_F_t12 = t12_P
    AND t03_F_t06 = t06_P
    AND t03_F_t07 = t07_P
    AND (t07_c027 = 8) AND ((t06_c006 = 4) AND (t06_c007 = 2) AND (t06_c005 = 6)) AND ((t13_c004 = 2) OR (t13_c005 = 2))
;
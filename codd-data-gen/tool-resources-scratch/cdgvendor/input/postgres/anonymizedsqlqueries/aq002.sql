SELECT * FROM
    t13,
    t12,
    t18,
    t07,
    t06
WHERE 1=1 
    AND t18_F_t13 = t13_P
    AND t18_F_t12 = t12_P
    AND t18_F_t06 = t06_P
    AND t18_F_t07 = t07_P
    AND ((t13_c004 = 2) OR (t13_c005 = 2)) AND (t07_c027 = 8) AND ((t06_c006 = 4) AND (t06_c007 = 4) AND (t06_c005 = 2))
;
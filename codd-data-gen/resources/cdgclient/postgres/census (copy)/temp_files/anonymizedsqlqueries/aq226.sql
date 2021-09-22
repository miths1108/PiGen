SELECT * FROM
    t13,
    t20,
    t08
WHERE 1=1 
    AND t13_F_t08 = t08_P
    AND t13_F_t20 = t20_P
    AND ((t13_c001 = 42) OR (t13_c001 = 4)) AND (t20_c008 > 26)
;
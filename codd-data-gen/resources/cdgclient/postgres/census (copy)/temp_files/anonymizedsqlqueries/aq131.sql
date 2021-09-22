SELECT * FROM
    t08,
    t13,
    t20
WHERE 1=1 
    AND t13_F_t20 = t20_P
    AND t13_F_t08 = t08_P
    AND (t08_c001 = 8) AND (t13_c001 = 28) AND (t20_c008 > 26)
;
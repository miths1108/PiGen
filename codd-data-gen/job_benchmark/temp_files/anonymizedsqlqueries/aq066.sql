SELECT * FROM
    t13,
    t08,
    t20
WHERE 1=1 
    AND t13_F_t20 = t20_P
    AND t13_F_t08 = t08_P
    AND (t08_c001 = 16) AND ((t20_c008 >= 16) AND (t20_c008 <= 24))
;
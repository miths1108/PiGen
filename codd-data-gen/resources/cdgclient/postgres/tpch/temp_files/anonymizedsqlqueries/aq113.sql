SELECT * FROM
    t13,
    t20,
    t10,
    t08
WHERE 1=1 
    AND t13_F_t08 = t08_P
    AND t13_F_t20 = t20_P
    AND t20_F_t10 = t10_P
    AND (t20_c008 > 14) AND (t10_c001 = 4) AND (t08_c001 = 16)
;
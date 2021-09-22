SELECT * FROM
    t13,
    t08,
    t20,
    t10
WHERE 1=1 
    AND t13_F_t20 = t20_P
    AND t13_F_t08 = t08_P
    AND t20_F_t10 = t10_P
    AND (t08_c001 = 16) AND (t20_c008 > 8) AND ((t10_c001 = 4) OR (t10_c001 = 6) OR (t10_c001 = 12) OR (t10_c001 = 10))
;
SELECT * FROM
    t13,
    t20,
    t10,
    t08
WHERE 1=1 
    AND t13_F_t08 = t08_P
    AND t13_F_t20 = t20_P
    AND t20_F_t10 = t10_P
    AND ((t13_c001 = 36) OR (t13_c001 = 30) OR (t13_c001 = 26) OR (t13_c001 = 16) OR (t13_c001 = 38) OR (t13_c001 = 12) OR (t13_c001 = 32) OR (t13_c001 = 24) OR (t13_c001 = 42) OR (t13_c001 = 6)) AND (t20_c008 > 16) AND ((t10_c001 = 4) OR (t10_c001 = 2)) AND (t08_c001 = 6)
;
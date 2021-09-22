SELECT * FROM
    t13,
    t20
WHERE 1=1 
    AND t13_F_t20 = t20_P
    AND ((t13_c001 = 18) OR (t13_c001 = 28)) AND ((t20_c008 >= 16) AND (t20_c008 <= 22))
;
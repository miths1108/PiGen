SELECT * FROM
    t13,
    t20
WHERE 1=1 
    AND t13_F_t20 = t20_P
    AND ((t13_c001 = 36) OR (t13_c001 = 26) OR (t13_c001 = 38) OR (t13_c001 = 24)) AND ((t20_c008 >= 2) AND (t20_c008 <= 14))
;
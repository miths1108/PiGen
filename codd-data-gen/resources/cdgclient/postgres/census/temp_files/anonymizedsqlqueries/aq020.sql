SELECT * FROM
    t13,
    t20
WHERE 1=1 
    AND t13_F_t20 = t20_P
    AND ((t13_c001 = 18) OR (t13_c001 = 28) OR (t13_c001 = 46) OR (t13_c001 = 22)) AND ((t20_c008 >= 14) AND (t20_c008 <= 26))
;
SELECT * FROM
    t13,
    t20,
    t08
WHERE 1=1 
    AND t13_F_t08 = t08_P
    AND t13_F_t20 = t20_P
    AND ((t20_c008 >= 14) AND (t20_c008 <= 26) AND (t20_c011 = 6)) AND (t08_c001 = 16)
;
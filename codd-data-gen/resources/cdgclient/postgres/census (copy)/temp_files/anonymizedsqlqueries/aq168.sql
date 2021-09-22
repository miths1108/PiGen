SELECT * FROM
    t05,
    t12,
    t20
WHERE 1=1 
    AND t12_F_t05 = t05_P
    AND t12_F_t20 = t20_P
    AND (t05_c000 = 14) AND ((t20_c008 >= 14) AND (t20_c008 <= 26) AND (t20_c011 = 6))
;
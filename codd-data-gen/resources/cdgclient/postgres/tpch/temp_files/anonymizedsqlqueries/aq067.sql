SELECT * FROM
    t12,
    t20,
    t05
WHERE 1=1 
    AND t12_F_t05 = t05_P
    AND t12_F_t20 = t20_P
    AND ((t12_c004 < 2) OR (t12_c004 > 2)) AND ((t20_c008 >= 16) AND (t20_c008 <= 24)) AND (t05_c000 = 14)
;
SELECT * FROM
    t20,
    t12,
    t05
WHERE 1=1 
    AND t12_F_t20 = t20_P
    AND t12_F_t05 = t05_P
    AND t05_c000 = 6
;
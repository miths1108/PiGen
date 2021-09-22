SELECT * FROM
    t05,
    t20,
    t10,
    t12,
    t06
WHERE 1=1 
    AND t12_F_t05 = t05_P
    AND t12_F_t20 = t20_P
    AND t20_F_t10 = t10_P
    AND t12_F_t06 = t06_P
    AND (t05_c000 = 14) AND (t10_c001 = 4) AND (t06_c001 = 4)
;
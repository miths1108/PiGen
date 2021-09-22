SELECT * FROM
    t20,
    t12,
    t06,
    t05,
    t10
WHERE 1=1 
    AND t20_F_t10 = t10_P
    AND t12_F_t20 = t20_P
    AND t12_F_t05 = t05_P
    AND t12_F_t06 = t06_P
    AND (t06_c001 = 4) AND (t05_c000 = 2) AND (t10_c001 = 4)
;
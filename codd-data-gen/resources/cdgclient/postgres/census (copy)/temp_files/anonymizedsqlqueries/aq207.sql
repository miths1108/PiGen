SELECT * FROM
    t05,
    t12,
    t20,
    t10
WHERE 1=1 
    AND t12_F_t05 = t05_P
    AND t12_F_t20 = t20_P
    AND t20_F_t10 = t10_P
    AND (t20_c008 = 20) AND (t10_c001 = 8)
;
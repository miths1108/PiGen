SELECT * FROM
    t12,
    t20,
    t10,
    t05
WHERE 1=1 
    AND t12_F_t05 = t05_P
    AND t12_F_t20 = t20_P
    AND t20_F_t10 = t10_P
    AND ((t20_c008 >= 16) AND (t20_c008 <= 22)) AND (t10_c001 = 8)
;
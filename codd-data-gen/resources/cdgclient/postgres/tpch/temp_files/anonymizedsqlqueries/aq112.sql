SELECT * FROM
    t12,
    t20,
    t10,
    t05,
    t06
WHERE 1=1 
    AND t12_F_t06 = t06_P
    AND t12_F_t05 = t05_P
    AND t12_F_t20 = t20_P
    AND t20_F_t10 = t10_P
    AND (t20_c008 > 16) AND ((t10_c001 = 4) OR (t10_c001 = 2)) AND ((t05_c000 < 14) OR (t05_c000 > 14))
;
SELECT * FROM
    t12,
    t05,
    t06,
    t20
WHERE 1=1 
    AND t12_F_t20 = t20_P
    AND t12_F_t06 = t06_P
    AND t12_F_t05 = t05_P
    AND ((t12_c004 < 2) OR (t12_c004 > 2)) AND ((t05_c000 < 8) OR (t05_c000 > 8)) AND ((t06_c001 < 4) OR (t06_c001 > 4)) AND (t20_c008 > 2)
;
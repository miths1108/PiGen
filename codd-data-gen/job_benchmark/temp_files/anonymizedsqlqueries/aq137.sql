SELECT * FROM
    t20,
    t10,
    t17,
    t02,
    t03
WHERE 1=1 
    AND t02_F_t20 = t20_P
    AND t20_F_t10 = t10_P
    AND t02_F_t17 = t17_P
    AND t02_F_t03 = t03_P
    AND (t20_c008 > 14) AND (t10_c001 = 4) AND (t03_c004 = 24)
;
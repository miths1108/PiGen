SELECT * FROM
    t20,
    t02,
    t03,
    t19
WHERE 1=1 
    AND t02_F_t19 = t19_P
    AND t02_F_t20 = t20_P
    AND t02_F_t03 = t03_P
    AND (t20_c008 > 26) AND ((t03_c004 = 12) OR (t03_c004 = 10)) AND (t19_c001 = 2)
;
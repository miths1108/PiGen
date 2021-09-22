SELECT * FROM
    t20,
    t02,
    t03,
    t17,
    t19
WHERE 1=1 
    AND t02_F_t19 = t19_P
    AND t02_F_t17 = t17_P
    AND t02_F_t20 = t20_P
    AND t02_F_t03 = t03_P
    AND (t20_c008 > 14) AND ((t02_c002 = 12) OR (t02_c002 = 18) OR (t02_c002 = 14) OR (t02_c002 = 16)) AND ((t03_c004 = 12) OR (t03_c004 = 10) OR (t03_c004 = 20)) AND (t17_c000 = 2) AND (t19_c001 = 4)
;
SELECT * FROM
    t17,
    t02,
    t20,
    t19
WHERE 1=1 
    AND t02_F_t17 = t17_P
    AND t02_F_t19 = t19_P
    AND t02_F_t20 = t20_P
    AND (t02_c002 = 16) AND ((t20_c008 >= 18) AND (t20_c008 <= 20)) AND (t19_c001 = 4)
;
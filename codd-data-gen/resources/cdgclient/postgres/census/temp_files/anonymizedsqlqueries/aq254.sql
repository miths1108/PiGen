SELECT * FROM
    t02,
    t19,
    t17,
    t20
WHERE 1=1 
    AND t02_F_t20 = t20_P
    AND t02_F_t17 = t17_P
    AND t02_F_t19 = t19_P
    AND t19_c001 = 6
;
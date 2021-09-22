SELECT * FROM
    t02,
    t17,
    t20
WHERE 1=1 
    AND t02_F_t20 = t20_P
    AND t02_F_t17 = t17_P
    AND ((t02_c002 = 6) OR (t02_c002 = 2)) AND (t17_c000 = 4)
;
SELECT * FROM
    t02,
    t17,
    t20
WHERE 1=1 
    AND t02_F_t20 = t20_P
    AND t02_F_t17 = t17_P
    AND (t02_c002 = 20) OR (t02_c002 = 4) OR (t02_c002 = 22) OR (t02_c002 = 10) OR (t02_c002 = 8)
;
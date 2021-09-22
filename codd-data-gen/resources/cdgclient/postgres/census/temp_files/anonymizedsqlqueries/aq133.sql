SELECT * FROM
    t02,
    t20,
    t17
WHERE 1=1 
    AND t02_F_t17 = t17_P
    AND t02_F_t20 = t20_P
    AND ((t02_c002 = 20) OR (t02_c002 = 4) OR (t02_c002 = 22) OR (t02_c002 = 10) OR (t02_c002 = 8)) AND (t20_c008 > 26) AND (t17_c000 = 4)
;
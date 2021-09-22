SELECT * FROM
    t02,
    t20,
    t17
WHERE 1=1 
    AND t02_F_t17 = t17_P
    AND t02_F_t20 = t20_P
    AND ((t20_c008 >= 4) AND (t20_c008 <= 6)) AND (t17_c000 = 4)
;
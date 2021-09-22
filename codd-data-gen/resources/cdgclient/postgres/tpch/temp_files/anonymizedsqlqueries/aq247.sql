SELECT * FROM
    t02,
    t17,
    t20
WHERE 1=1 
    AND t02_F_t20 = t20_P
    AND t02_F_t17 = t17_P
    AND ((t17_c006 >= 2) AND (t17_c006 <= 4) AND ((t17_c000 = 4) OR (t17_c000 = 2))) AND ((t20_c008 >= 4) AND (t20_c008 <= 26))
;
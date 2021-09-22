SELECT * FROM
    t00,
    t17
WHERE 1=1 
    AND t00_F_t17 = t17_P
    AND (t17_c006 >= 2) AND (t17_c006 <= 4) AND ((t17_c000 = 4) OR (t17_c000 = 2))
;
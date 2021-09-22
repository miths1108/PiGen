SELECT * FROM
    t23,
    t15,
    t07,
    t19,
    t05
WHERE 1=1 
    AND t23_F_t05 = t05_P
    AND t23_F_t19 = t19_P
    AND t23_F_t07 = t07_P
    AND t23_F_t15 = t15_P
    AND ((t15_c000 = 2) OR (t15_c000 = 4)) AND ((t07_c027 = 4) OR (t07_c020 = 4) OR (t07_c019 = 16)) AND ((t19_c007 >= 2) AND (t19_c007 <= 4)) AND (t05_c005 = 2)
;
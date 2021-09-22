SELECT * FROM
    t19,
    t03,
    t07,
    t15
WHERE 1=1 
    AND t03_F_t19 = t19_P
    AND t03_F_t15 = t15_P
    AND t03_F_t07 = t07_P
    AND ((t19_c007 >= 2) AND (t19_c007 <= 4)) AND (t07_c027 = 4) AND ((t15_c000 = 2) OR (t15_c000 = 4))
;
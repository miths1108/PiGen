SELECT * FROM
    t23,
    t07,
    t12,
    t05
WHERE 1=1 
    AND t23_F_t05 = t05_P
    AND t23_F_t12 = t12_P
    AND t23_F_t07 = t07_P
    AND ((t07_c027 = 6) AND (t07_c019 = 4)) AND (t12_c002 = 8) AND (t05_c005 = 4)
;
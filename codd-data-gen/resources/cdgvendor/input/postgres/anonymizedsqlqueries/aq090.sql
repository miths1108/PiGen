SELECT * FROM
    t05,
    t12,
    t23,
    t07
WHERE 1=1 
    AND t23_F_t05 = t05_P
    AND t23_F_t12 = t12_P
    AND t23_F_t07 = t07_P
    AND (t05_c005 = 4) AND ((t07_c027 = 2) AND (t07_c019 = 2))
;
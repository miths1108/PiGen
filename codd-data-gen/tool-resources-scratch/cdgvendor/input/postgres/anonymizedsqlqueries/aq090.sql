SELECT * FROM
    t12,
    t05,
    t03,
    t07
WHERE 1=1 
    AND t03_F_t12 = t12_P
    AND t03_F_t05 = t05_P
    AND t03_F_t07 = t07_P
    AND (t05_c005 = 4) AND ((t07_c027 = 2) AND (t07_c019 = 2))
;
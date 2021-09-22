SELECT * FROM
    t05,
    t03,
    t07,
    t12
WHERE 1=1 
    AND t03_F_t05 = t05_P
    AND t03_F_t12 = t12_P
    AND t03_F_t07 = t07_P
    AND (t05_c005 = 4) AND ((t07_c027 = 6) AND (t07_c019 = 4)) AND (t12_c002 = 8)
;
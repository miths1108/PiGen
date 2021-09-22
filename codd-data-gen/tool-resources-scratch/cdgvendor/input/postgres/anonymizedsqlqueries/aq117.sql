SELECT * FROM
    t03,
    t07,
    t12
WHERE 1=1 
    AND t03_F_t12 = t12_P
    AND t03_F_t07 = t07_P
    AND (t07_c027 >= 4) AND (t07_c027 <= 8)
;
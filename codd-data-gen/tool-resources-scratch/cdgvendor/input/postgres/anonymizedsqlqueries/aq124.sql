SELECT * FROM
    t12,
    t23,
    t07
WHERE 1=1 
    AND t23_F_t12 = t12_P
    AND t23_F_t07 = t07_P
    AND (t07_c027 = 10) AND (t07_c019 = 14)
;
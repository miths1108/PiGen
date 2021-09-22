SELECT * FROM
    t12,
    t03,
    t07
WHERE 1=1 
    AND t03_F_t12 = t12_P
    AND t03_F_t07 = t07_P
    AND (t07_c027 = 4) OR ((t07_c027 = 2) AND (t07_c019 = 16)) OR ((t07_c027 = 6) AND (t07_c019 = 2))
;
SELECT * FROM
    t03,
    t07
WHERE 1=1 
    AND t03_F_t07 = t07_P
    AND (t07_c020 < 6) AND (t07_c027 = 8)
;
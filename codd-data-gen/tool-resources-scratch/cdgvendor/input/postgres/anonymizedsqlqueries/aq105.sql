SELECT * FROM
    t23,
    t07
WHERE 1=1 
    AND t23_F_t07 = t07_P
    AND (t07_c027 = 2) OR (t07_c027 = 4)
;
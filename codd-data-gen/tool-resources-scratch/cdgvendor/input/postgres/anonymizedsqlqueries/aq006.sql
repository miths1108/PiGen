SELECT * FROM
    t23,
    t07,
    t12
WHERE 1=1 
    AND t23_F_t12 = t12_P
    AND t23_F_t07 = t07_P
    AND (t07_c018 >= 44) AND (t07_c018 <= 58)
;
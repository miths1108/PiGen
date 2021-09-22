SELECT * FROM
    t23,
    t07,
    t05,
    t24
WHERE 1=1 
    AND t23_F_t24 = t24_P
    AND t23_F_t05 = t05_P
    AND t23_F_t07 = t07_P
    AND ((t07_c005 >= 14) AND (t07_c005 <= 22)) AND (t05_c007 = 14) AND (t24_c004 = 2)
;
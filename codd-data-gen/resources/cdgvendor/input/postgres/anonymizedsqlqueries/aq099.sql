SELECT * FROM
    t22,
    t07
WHERE 1=1 
    AND t22_F_t07 = t07_P
    AND (t07_c005 >= 2) AND (t07_c005 <= 4)
;
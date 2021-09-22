SELECT * FROM
    t18,
    t07
WHERE 1=1 
    AND t18_F_t07 = t07_P
    AND (t07_c018 >= 2) AND (t07_c018 <= 8)
;
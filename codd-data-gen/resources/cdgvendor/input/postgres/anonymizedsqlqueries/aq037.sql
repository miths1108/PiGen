SELECT * FROM
    t12,
    t18,
    t07
WHERE 1=1 
    AND t18_F_t12 = t12_P
    AND t18_F_t07 = t07_P
    AND (t07_c018 >= 50) AND (t07_c018 <= 62)
;
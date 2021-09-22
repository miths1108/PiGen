SELECT * FROM
    t18,
    t07,
    t12
WHERE 1=1 
    AND t18_F_t12 = t12_P
    AND t18_F_t07 = t07_P
    AND ((t07_c005 >= 6) AND (t07_c005 <= 8)) AND ((t12_c002 = 14) OR (t12_c002 = 10) OR (t12_c002 = 20))
;
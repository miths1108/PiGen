SELECT * FROM
    t11,
    t07,
    t12,
    t20
WHERE 1=1 
    AND t11_F_t20 = t20_P
    AND t11_F_t12 = t12_P
    AND t11_F_t07 = t07_P
    AND ((t07_c005 >= 30) AND (t07_c005 <= 32)) AND ((t12_c008 >= 2) AND (t12_c008 <= 4))
;
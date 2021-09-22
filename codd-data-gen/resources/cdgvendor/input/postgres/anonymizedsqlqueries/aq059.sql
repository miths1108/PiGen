SELECT * FROM
    t11,
    t07,
    t12,
    t20
WHERE 1=1 
    AND t11_F_t20 = t20_P
    AND t11_F_t12 = t12_P
    AND t11_F_t07 = t07_P
    AND (t07_c018 >= 20) AND (t07_c018 <= 42)
;
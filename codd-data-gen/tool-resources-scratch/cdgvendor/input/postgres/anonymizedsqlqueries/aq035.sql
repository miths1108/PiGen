SELECT * FROM
    t11,
    t07,
    t03,
    t12
WHERE 1=1 
    AND t11_F_t12 = t12_P
    AND t11_F_t07 = t07_P
    AND t03_F_t12 = t12_P
    AND ((t11_c002 >= 2) AND (t11_c002 <= 4)) AND ((t07_c005 >= 34) AND (t07_c005 <= 38)) AND ((t12_c008 >= 6) AND (t12_c008 <= 8) AND ((t12_c015 = 8) OR (t12_c015 = 6) OR (t12_c015 = 16) OR (t12_c015 = 18)))
;
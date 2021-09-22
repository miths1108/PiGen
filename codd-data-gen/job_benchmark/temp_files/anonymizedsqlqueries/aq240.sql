SELECT * FROM
    t20,
    t16,
    t11
WHERE 1=1 
    AND t16_F_t20 = t20_P
    AND t16_F_t11 = t11_P
    AND ((t20_c008 >= 4) AND (t20_c008 <= 10)) AND (t11_c001 = 8)
;
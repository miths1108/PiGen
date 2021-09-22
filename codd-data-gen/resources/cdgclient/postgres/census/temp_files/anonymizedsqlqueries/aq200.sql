SELECT * FROM
    t20,
    t20,
    t10,
    t16,
    t11
WHERE 1=1 
    AND t16_F_t20 = t20_P
    AND t16_F_t20 = t20_P
    AND t20_F_t10 = t10_P
    AND t16_F_t11 = t11_P
    AND ((t20_c008 >= 16) AND (t20_c008 <= 22)) AND (t10_c001 = 8) AND ((t11_c001 = 6) OR (t11_c001 = 4) OR (t11_c001 = 2))
;
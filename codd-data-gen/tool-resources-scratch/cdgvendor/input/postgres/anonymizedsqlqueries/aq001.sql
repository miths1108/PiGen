SELECT * FROM
    t18,
    t16,
    t09,
    t19
WHERE 1=1 
    AND t18_F_t19 = t19_P
    AND t18_F_t09 = t09_P
    AND t18_F_t16 = t16_P
    AND (t16_c021 = 2) AND (t09_c002 = 10) AND ((t19_c003 >= 2) AND (t19_c001 = 2))
;
SELECT * FROM
    t23,
    t21,
    t19,
    t09
WHERE 1=1 
    AND t23_F_t09 = t09_P
    AND t23_F_t19 = t19_P
    AND t23_F_t21 = t21_P
    AND ((t21_c002 >= 2) AND (t21_c002 <= 4)) AND ((t19_c001 >= 8) AND (t19_c001 <= 10)) AND (t09_c002 = 12)
;
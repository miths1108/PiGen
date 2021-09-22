SELECT * FROM
    t23,
    t21,
    t09,
    t19
WHERE 1=1 
    AND t23_F_t19 = t19_P
    AND t23_F_t09 = t09_P
    AND t23_F_t21 = t21_P
    AND ((t21_c002 >= 2) AND (t21_c002 <= 4)) AND (t09_c002 = 12) AND ((t19_c001 >= 12) AND (t19_c001 <= 14))
;
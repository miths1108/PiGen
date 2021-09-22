SELECT * FROM
    t18,
    t07,
    t12
WHERE 1=1 
    AND t18_F_t12 = t12_P
    AND t18_F_t07 = t07_P
    AND (t07_c027 = 10) AND ((((t12_c002 = 10) OR (t12_c002 = 20) OR (t12_c002 = 16)) AND ((t12_c004 = 12) OR (t12_c004 = 6) OR (t12_c004 = 10))) OR (((t12_c002 = 12) OR (t12_c002 = 18) OR (t12_c002 = 14)) AND ((t12_c004 = 30) OR (t12_c004 = 26) OR (t12_c004 = 18))))
;
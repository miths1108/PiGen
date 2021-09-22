SELECT * FROM
    t08,
    t13
WHERE 1=1 
    AND t13_F_t08 = t08_P
    AND (t08_c001 = 8) AND ((t13_c001 = 18) OR (t13_c001 = 28) OR (t13_c001 = 46) OR (t13_c001 = 22))
;
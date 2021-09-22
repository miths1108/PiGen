SELECT * FROM
    t18,
    t08,
    t17
WHERE 1=1 
    AND t18_F_t17 = t17_P
    AND t18_F_t08 = t08_P
    AND (t08_c001 = 10) AND (t17_c000 = 2)
;
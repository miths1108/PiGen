SELECT * FROM
    t17,
    t08,
    t18
WHERE 1=1 
    AND t18_F_t17 = t17_P
    AND t18_F_t08 = t08_P
    AND (t17_c000 = 4) AND (t08_c001 = 12) AND (t18_c003 = 4)
;
-- query 16 (rerun)

select *
from
	supplier
where
	-- s_comment LIKE '%Customer%Complaint%'
	s_comment in ('arefully aboCustomer gular packages. idly regular requests booComplaintssly fluff','Customer Complaintshe slyly','Customer nts. furComplaints','ans. ironicCustomer  requests cajole carefullyComplaintsy regular reque')
;

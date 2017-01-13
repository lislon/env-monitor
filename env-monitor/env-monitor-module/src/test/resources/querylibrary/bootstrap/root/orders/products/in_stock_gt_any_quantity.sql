--TITLE  Best Query Ever =>)
--DESCRIPTION  Get Netting Group calculations in MCS Run by run context id 
--PARAM price:STRING
--PARAM quantity:NUMBER

SELECT * FROM PRODUCTS WHERE QUANTITY_IN_STOCK > :quantity AND BUY_PRICE > :price
--TITLE  In Stock > Any quantity
--DESCRIPTION  Some description with -- characters
--PARAM price:STRING
--PARAM quantity:NUMBER

SELECT * FROM PRODUCTS WHERE QUANTITY_IN_STOCK > :quantity AND BUY_PRICE > :price
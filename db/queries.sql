-- Select all forecast for tickers except those which were changed by analyst
SELECT f1.analyst, f1.ticker_id, f1.target_price, f1.publish_date
FROM forecasts f1
    INNER JOIN (
        SELECT MAX(f2.publish_date) AS max_date, f2.analyst, f2.ticker_id
        FROM forecasts f2
--         use hibernate to make date
        WHERE f2.publish_date > make_date(2020, 7, 15)
        GROUP BY f2.analyst, f2.ticker_id) f2
    ON f1.analyst = f2.analyst AND f1.ticker_id = f2.ticker_id AND f1.publish_date = f2.max_date
ORDER BY f1.ticker_id, f1.analyst;

-- Select all tickers with changed forecasts (CVX 105.85 - 105.83, RACE 183 - 179.25)
SELECT l2.consensus, l1.ticker_id, l1.consensus
FROM live_data l1 INNER JOIN live_data l2 ON l1.ticker_id = l2.ticker_id AND l2.date = make_date(2020, 08, 14)
WHERE (l1.date = make_date(2020, 08, 15) AND (l1.consensus != l2.consensus OR (l2.consensus is not null AND l1.consensus is null)))
ORDER BY l1.ticker_id;

### Simple JBang script to retrieve ticker

Retrives last 31 day of a ticker from http://marketstack.com
To run you need JBang installed on your sistem.

Usage example:
```sh
jbang stocker.java -A <accesskey> 2022-09-08
```

or use environment variable:

```sh
export MARKET_STOCK_API_KEY=blabla
jbang stocker.java 2022-09-08
```


## Github action usage
- go to `Actions` tab
- select `Retrieve stock`
- run workflow
- once the action completed the results are printed to stdout as CSV in the section `jbang`
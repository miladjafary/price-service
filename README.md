# Price Service

Price Service is a project comprises simple JAVA API for keeping track of the last price for financial instruments.

## In Memory Price Service
The [https://github.com/miladjafary/price-service/blob/main/src/main/java/com/miladjafari/price/InMemoryPriceService.java](InMemoryPriceService) is an 
implementation of [https://github.com/miladjafary/price-service/blob/main/src/main/java/com/miladjafari/price/PriceService.java](PriceService) in which store the prices 
in a memory. This implementation support transactional operation meaning that you could open a transaction for each individual threads and either commit 
the new list of prices or rollback the changes. the following code snippet show how to use it for updating the prices. 

```java
PriceService priceService = new InMemoryPriceService();
priceService.beginBatchTransaction();
PriceDto priceDto = new PriceDto(UUID.randomUUID().toString(), LocalDateTime.now().plusMinutes(10), 1000D);
priceService.save(priceDto);
priceService.commitBatchTransaction();

```


## Javadoc
Your Javadoc can be generated by using: 
```shell script
mvn install
```
Afterward, the Javadoc will be available on `target/apidocs/index.html`

## Code Coverage 
In this project JaCoCo Maven plugin is used to generate a code coverage report. You can generate report by using:
```shell script
mvn clean test
```
The code coverage report will be generated at `target/site/jacoco/*`. Open `target/site/jacoco/index.html` file, review the code coverage report

# Price Service

Price Service is a project comprises simple JAVA API for keeping track of the last price for financial instruments.

## In Memory Price Service
The [InMemoryPriceService](https://github.com/miladjafary/price-service/blob/main/src/main/java/com/miladjafari/price/InMemoryPriceService.java) is an 
implementation of [PriceService](https://github.com/miladjafary/price-service/blob/main/src/main/java/com/miladjafari/price/PriceService.java) in which store the prices 
in a memory. This implementation support transactional operation meaning that you could open a transaction for each individual threads and either commit 
the new list of prices or rollback the changes. the following code snippet show how to use it for updating the prices. 

```java
PriceDto priceDto = new PriceDto(UUID.randomUUID().toString(), LocalDateTime.now().plusMinutes(10), 1000D);
PriceService priceService = new InMemoryPriceService();

priceService.beginBatchTransaction();
priceService.save(priceDto);
priceService.commitBatchTransaction();

// or you could roll back the transaction by collection the
priceService.rollBackBatchTransaction();
```
Since the transaction bind to each thread, modifying on the prices list will not affect on the main price list date. 
Once the `commitBatchTransaction()` is called, a `ReadWriteLock` is acquired to ensure that the consumer threads do not 
read dirty data. 

## Producer
The [Producer](https://github.com/miladjafary/price-service/blob/main/src/main/java/com/miladjafari/helper/Producer.java) and
the [Consumer](https://github.com/miladjafary/price-service/blob/main/src/main/java/com/miladjafari/helper/Consumer.java) are developed to 
facilitate uploading or reading the latest prices. An example of these two classes are mentioned in the following:
```java
//Create an instance of InMemoryPriceService
PriceService priceService = new InMemoryPriceService();

//Create a bunch of PriceDto list
List<PriceDto> prices = createPriceLists(200);

//Create Producer and upload the prices
Producer producer = new Producer(priceService);
producer.uploadPrices(prices);

//Create consumers 
String priceId= prices.get(0).getId();
Optional<PriceDto> priceOptional =  new Consumer(priceService).getLatestPriceById(priceId);
priceOptional.ifPresent(price->{
    logger.info("Price Id:" + price.getId());
    logger.info("Price AsOf: "+ price.getAsOf());
    logger.info("Price value: "+ price.getPrice());
});
```

## Javadoc
Your Javadoc can be generated by using: 
```shell script
mvn install
```
Afterward, the Javadoc will be available on `target/apidocs/index.html`

# Food Analyzer

I have implemented Food Analyzer as a multi-threaded client-server application.

## Food Analyzer Server

- The server must be able to serve multiple clients simultaneously.
- The server receives commands from clients and returns an appropriate result.
- The server retrieves the data it needs from the used *RESTful API* and saves (caches) the result in its local file system. 

    For example, upon receiving the command `get-food raffaello treat`, the server makes the following *HTTP GET* request: https://api.nal.usda.gov/fdc/v1/foods/search?query=raffaello%20treat&requireAllWords=true&api_key=API_KEY (where API_KEY is a valid API key) and receives an *HTTP response* with status code *200* and body the following *JSON*:

```javascript
{
  "foodSearchCriteria": {
    "query": "raffaello treat",
    "generalSearchInput": "raffaello treat",
    "pageNumber": 1,
    "requireAllWords": true
  },
  "totalHits": 1,
  "currentPage": 1,
  "totalPages": 1,
  "foods": [
    {
      "fdcId": 415269,
      "description": "RAFFAELLO, ALMOND COCONUT TREAT",
      "dataType": "Branded",
      "gtinUpc": "009800146130",
      "publishedDate": "2019-04-01",
      "brandOwner": "Ferrero U.S.A., Incorporated",
      "ingredients": "VEGETABLE OILS (PALM AND SHEANUT). DRY COCONUT, SUGAR, ALMONDS, SKIM MILK POWDER, WHEY POWDER (MILK), WHEAT FLOUR, NATURAL AND ARTIFICIAL FLAVORS, LECITHIN AS EMULSIFIER (SOY), SALT, SODIUM BICARBONATE AS LEAVENING AGENT.",
      "allHighlightFields": "",
      "score": 247.10071
    }
  ]
}
```

Requests to the REST API require API key authentication, which you can get by registering [here](https://fdc.nal.usda.gov/api-key-signup.html).

From the product data, we are interested in the description of the product from the `description` field (`RAFFAELLO, ALMOND COCONUT TREAT`) and its unique identifier, `fdcId` (`415269`). Some products, specifically those with `data type` `Branded`, also have a GTIN or UPC code, `gtinUpc` (`009800146130`).

The server caches the received information on the local file system. Upon receiving a request, the server must first check to see if information about the given product already exists in the cache, and if so, return that information directly instead of making a new request to the REST API.

## Food Analyzer Client

The client connects to the *Food Analyzer Server* on a specific port, reads commands from the standard input, sends them to the server, and outputs the result in human-readable format. The client can execute the following commands:

- `get-food <food_name>` - displays the described above information for a food product. If the server returns multiple products with the given name, information about each is displayed. Alternatively, if there is no information for the product, an appropriate message is output.
- `get-food-report <food_fdcId>` - by a given unique product identifier (`fdcId`) displays product name, ingredients (`ingredients`), energy value (calories), protein, fat, carbohydrate and fiber content.

### Example of valid input

```bash
get-food beef noodle soup
get-food-report 415269
```

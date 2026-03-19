# ApiSynIQ

**ApiSynIQ** is an open-source framework that lets users interact with your APIs through natural voice or text instead of filling long frontend forms.  
It understands user intent, fills the required JSON bodies, query parameters, and headers automatically, calls the right API endpoints, and interprets the responses—making API interactions fast, simple, and conversational.

Integrate ApiSynIQ into your application to turn complex workflows like booking tickets, updating user data, or searching products into effortless voice or chat commands.

## Development Approach

![Descriptive alt text for the image](Architecture/ProductionGrade.jpeg)

### Here is the breakdown of **how a user request flows?**

**GO Gateway**
Whenever a user queries the application's frontend, the request reaches the GO gateway. GO Gateway handles all the load and distributes it to the AI workload. Here, the GO language is used specifically to handle concurrent requests. Go handles all the HTTP-based and WebSocket-based requests seamlessly. 

This application uses
**net/http** for HTTP request, as it consists of all the required features.
**gorilla** for WebSocket-based requests, which allows us to maintain flowless streaming of AI tokens.



# multi-module-architecture
This project is a multi module maven code sample that is inspired by clean architecture and hexagonal architecture, but is it's own kind of breed.

![hexagonal_like_architecture.png](doc/hexagonal_like_architecture.png)

# Usage

# Build
During build, [lombok][20] needs to be enabled to do preprocessing
Build the project like any other maven build with `mvn clean package`


# Integration
The integration to the public [Advice API][50] is done through REST calls.

The services from the maven module for advice API is only available through a domain interface and all object models from the external API
is maintained in the integration module, only to be mapped into the domain model made available for all other maven modules through domain module.


[20]: https://projectlombok.org/
[50]:https://api.adviceslip.com/#top

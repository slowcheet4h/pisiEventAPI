# pisiEventAPI
NOTE: this project requires MeowLib<br>
you can get it here: https://github.com/united-meows/MeowLib
<br>
## Usage
# example event class<br>
![image](https://user-images.githubusercontent.com/47327665/114080692-57d45300-98b4-11eb-8a09-b6732401278b.png)
<br>
<br>
## if you want to use async events<br>
![image](https://user-images.githubusercontent.com/47327665/114080809-7b979900-98b4-11eb-923d-4bcf3a3240c8.png)
## NOTE: Async events can't get canceled/stopped
## Creating a Event System and Registering Events
![image](https://user-images.githubusercontent.com/47327665/114081126-da5d1280-98b4-11eb-8a98-ea1631beffff.png)
## creating a Listener
![image](https://user-images.githubusercontent.com/47327665/114081768-a3d3c780-98b5-11eb-8c61-47abfba37ac6.png)
<br>
events(*): the events that listener listens (multiple event support)
<br>
ignoreCanceled: if event is canceled before, the method wont get called (default: false)
<br>
autoregister: should "registerAll" method register the listener (default: true) (you can register manually with eventSystem.register( )
<br>
label: name of listener (only required for stopping/starting/pausing listener
<br>
weight: priority of listener  MASTER(10), HIGHEST(5) MEDIUM(3), LOW(2), LOWEST(1), SLAVE(-9), MONITOR(-10) (default: medium)
<br>

## Regitering Listeners
<br>
registerAll registers all methods with @Listener attribute <br>

```java
    eventSystem.registerAll(listenerClass); // registers all listeners in a class
    // or you can register manually
    eventSystem.register(listenerClass, "test_listener"); // listener should have a label
```
<br>

## Filters
![image](https://user-images.githubusercontent.com/47327665/114083350-856ecb80-98b7-11eb-98a7-f54dcf94766f.png)
<br>
if check returns true event will be filtered and method wont get called

## Stopping an event from calling other listeners
![image](https://user-images.githubusercontent.com/47327665/114084476-e34fe300-98b8-11eb-9beb-dad69bf20f92.png)
<br>
depends on priority, next listeners wont get that event


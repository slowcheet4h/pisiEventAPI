# pisiEventAPI
NOTE: Async listeners requires MeowLib (only required if you are going to use Async) <br>
you can get it here: https://github.com/united-meows/MeowLib<br>
<br>
## Usage
## example event class<br>
![testEvent](https://user-images.githubusercontent.com/47327665/139541443-bf792201-201d-4230-9be1-21933e9269b8.png)
<br>
<br>
## creating a event manager<br>
![Screenshot_20211030_185957](https://user-images.githubusercontent.com/47327665/139541261-91694ac7-b982-42ff-b369-30480598c987.png)
<br>
<br>
## creating a listener<br>
![listener](https://user-images.githubusercontent.com/47327665/144812588-212908f1-7cfe-4e99-8ff5-eb2570731f29.png)
<br>
<br>
# !! NOTE: for users using java version 8 or less<br>
Avoid using lambdas or it'll make classes load slowly (90-200ms per class)<br>
![java18](https://user-images.githubusercontent.com/47327665/144812651-f21e81bf-e737-4fb3-8587-1019f87b5bb3.png)
<br>
<i>Use anonymous calls instead</i>
## listener settings<br>
![weight](https://user-images.githubusercontent.com/47327665/144812731-7beaafa3-70c7-4e5e-a374-77241d6901c0.png)
<br>
<br>
## asynclistener and custom listeners<br>
![asynclistener](https://user-images.githubusercontent.com/47327665/144812858-7757efd5-ebab-4283-8555-f6c87cee2c58.png)
<br>
<i>Async listeners can't cancel or stop events and editing an event wont make difference (since its async)</i>
<br>
<br>
## registering listeners<br>
![Screenshot_20211030_190215](https://user-images.githubusercontent.com/47327665/139541767-3c6e526b-fb40-4f57-80a0-86a609ac367b.png)
<br>
<br>
## unregistering listeners<br>
![Screenshot_20211030_190306](https://user-images.githubusercontent.com/47327665/139541781-091320d7-1647-44ec-a422-aec6f5564a52.png)
<br>
<br>
<br>
Credits
<h3>! This event api contains features from Brady's event api</h3>

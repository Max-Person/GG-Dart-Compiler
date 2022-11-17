class Car {
	String make;
   	String model;
   	int yearMade;
   	bool hasABS;
   
   	Car(String make, String model, int year, bool hasABS) {
    	this.make = make;
      	this.model = model;
      	this.yearMade = year;
      	this.hasABS = hasABS;
   	}
    
    Car(String make, String model, int year, bool hasABS,) {
    	this.make = make;
      	this.model = model;
      	this.yearMade = year;
      	this.hasABS = hasABS;
   	}

    Car(this.make, this.model, this.yearMade, this.hasABS);
    Car(this.make, this.model, this.yearMade, this.hasABS,);
    
    Car.withoutABS(this.make, this.model, this.yearMade): hasABS = false;

    Car.withoutABS_2(this.make, this.model, this.yearMade): this(make, model, yearMade, false);
}

class FordFocus {
   static const FordFocus fordFocus = FordFocus("Ford", "Focus", "2013", true);
   
   final String make;
   final String model;
   final String yearMade;
   final bool hasABS;
   
   const FordFocus(this.make, this.model, this.yearMade, this.hasABS);
   
}

class Person{
  
    String name = "";
    int age = 0;
      
    Person(String userName,int userAge) : name=userName, age = userAge 
    {
        print("Person ctor!");
    }
}

class Customer {
  String? name;
  int? age;
  String? location;

  Customer(this.name, this.age, this.location);

Customer.withoutLocation(this.name, this.age);

Customer.empty() {
  name = "";
  age = 0;
  location = "";
}
   Customer.empty2() : this("", 0, "");
  Customer.withoutLocation2(String name, int age) : this.empty();
}
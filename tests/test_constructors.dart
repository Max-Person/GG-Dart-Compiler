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
      
    Person(userName, userAge) : name=userName, age = userAge 
    {
        print("Person ctor!");
    }
}
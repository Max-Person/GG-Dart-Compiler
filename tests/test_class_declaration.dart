class Person{ // объявление класса
  
}

class Employee extends Person{ // наследование
 
}

class Geek {  // класс интерфейс
   void printdata() { 
      print("Hello Geek !!"); 
   } 
}  
  
class Gfg implements Geek { // реализация интерфейса
   void printdata() {  
      print("Welcome to GeeksForGeeks"); 
   } 
}


class Geek1 { 
   void printdata1() { 
      print("Hello Geek1 !!"); 
   } 
}  

class Geek2 { 
   void printdata2() { 
      print("Hello Geek2 !!"); 
   } 
}  
  
class Gfg2 implements Geek1, Geek2{ // реализация нескольких интерфейсов
   void printdata1() {  
      print("Howdy Geek1,\nWelcome to GeeksForGeeks"); 
   } 
    
  void printdata2() {  
      print("Howdy Geek2,\nWelcome to GeeksForGeeks"); 
   }
} 

abstract class IsSilly { // абстрактный класс
  void makePeopleLaugh();
}

class Clown implements IsSilly { // реализация абстрактного класса (??)
  void makePeopleLaugh() {
    
  }
}

abstract class Figure { // абстрактный класс
    void calculateArea();
}

class Rectangle extends Figure{      // наследование от абстрактного класса 
    void calculateArea(){
    }
}

class A{
    
}

class C{

}

class D{

}

class E{

}

class F{

}

class B extends A implements C{

}

class B1 extends A implements C, D{

}

class B2 with D{

}

class B3 with C, D{

}

class B4 extends A with C, D implements E, F{
    
}
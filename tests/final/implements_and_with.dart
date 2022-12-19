void main(){
  
  Geek1 a = new Gfg2();
  Geek2 b = new Gfg2();
  print("a says:");
  a.printdata1();
  print("b says:");
  b.printdata2();
  print("c says:");
  Hater c = new Gfg2();
  c.hate();
  print("d says:");
  GeekHater d = new GeekHater();
  d.hate();
  d.printdata1();

  
}

class GeekHater = Geek1 with Hater;


class Hater {
  void hate(){
      print("I HATE!!!!"); 
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
  
class Gfg2  with Hater implements Geek1, Geek2{ // реализация нескольких интерфейсов
   void printdata1() {  
      print("Howdy Geek1,\nWelcome to GeeksForGeeks"); 
   } 
    
  void printdata2() {  
      print("Howdy Geek2,\nWelcome to GeeksForGeeks"); 
   }
} 

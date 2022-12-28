class Person{
  String? name;
  int? age;

  Person(this.name, this.age);
  
}

void main() {
  
  Person person = new Person("Человек", 12);

  print("имя после инициализации ${person.name}");
  print("возраст после инициализации " + person.age.toString() + "\n");

  Person person2 = person;
  changeNameAndAge(person2);

  print("имя после вызова метода ${person.name}");
  print("возраст после вызовы метода " + person.age.toString() + "\n");

  int a = 5;
  print("значение числа до передачи в функцию $a");
  intVal(a);
  print("значение числа после передачи в функции $a \n");

  String str = "new str";
  print("значение строки до передачи в функцию $str");
  strVal(str);
  print("значение строки после передачи в функцию $str \n");

}


void changeNameAndAge(Person person){
  print("Вызов метода changeNameAndAge");
    person.name = "Новое имя";
    person.age = 10;
}

void intVal(int a){
  print("вызов intVal");
  a = 1;
}

void strVal(String str){
  print("вызов strVal");
  str = "Строка";
}
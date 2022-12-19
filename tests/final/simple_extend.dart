//int a = 10;

void main(){
  
  Employee jessie = new Employee.anonymous();
  Person mrWhite = new Person("Heisenberg");
	print(jessie.sayMyName());
	print(mrWhite.sayMyName());
  
}


class Person{ // объявление класса
  String name;
  Person(this.name);
  Person.anonymous() : this("[anon]");

  String sayMyName(){
    return name;
  }
}

class Employee extends Person{ // наследование
  int jobId;
  Employee(int jobId, String name) : jobId = jobId, super(name);
  Employee.jobless(String name) : jobId = -1, super(name);
  Employee.anonymous() : jobId = -1, super.anonymous();

  String sayMyName(){
    return "Employee $name, jobID = $jobId";
  }
}

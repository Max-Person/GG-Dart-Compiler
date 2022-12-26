void main() {
  // Testing local variables
  print('Input a: ');
  var a = readInt();
  print('Input b: ');
  var b = readDouble();
  print('a = $a, b  = $b');
  
  // Testing arithmetic operations
  print('Adding a and b: ${a + b}'); // Output: Adding a and b: 30
  print('Subtracting b from a: ${a - b}'); // Output: Subtracting b from a: -10
  print('Multiplying a and b: ${a * b}'); // Output: Multiplying a and b: 200
  print('Dividing b by a: ${b / a}'); // Output: Dividing b by a: 2.0
  
  // Testing comparisons
  print('Checking if a is equal to b: ${a == b}'); // Output: Checking if a is equal to b: false
  print('Checking if a is not equal to b: ${a != b}'); // Output: Checking if a is not equal to b: true
  print('Checking if a is less than b: ${a < b}'); // Output: Checking if a is less than b: true
  print('Checking if a is less than or equal to b: ${a <= b}'); // Output: Checking if a is less than or equal to b: true
  print('Checking if b is greater than a: ${b > a}'); // Output: Checking if b is greater than a: true
  print('Checking if b is greater than or equal to a: ${b >= a}'); // Output: Checking if b is greater than or equal to a: true
  
  // Testing string interpolation and concatenation
  String name = 'Alice';
  int age = 30;
  print('My name is $name and I am $age years old'); // Output: My name is Alice and I am 30 years old
  print('Hello, ' + name + '! You are ' + age.toString() + ' years old. In ten years you\'ll be ${age + 10} years old'); // Output: Hello, Alice! You are 30 years old
}
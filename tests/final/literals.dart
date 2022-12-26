void main() {
  // Testing numeric constants
  int integer = 42;
  print(integer); // Output: 42
  
  double floatingPoint = 3.14;
  print(floatingPoint); // Output: 3.14
  
  int hexadecimal = 0x2A;
  print(hexadecimal); // Output: 42
  
  double exponential = 1.23e4;
  print(exponential); // Output: 12300.0
  
  // Testing boolean constants
  bool truth = true;
  print(truth); // Output: true
  
  bool falsehood = false;
  print(falsehood); // Output: false
  
  // Testing string constants
  String singleLine = 'Single line string';
  print(singleLine); // Output: Single line string
  
  String multiLine = '''
    This is a
    multi-line string
  ''';
  print(multiLine); // Output: This is a\nmulti-line string
  
  String specialCharacters = 'This string contains special characters: \n, \t, and \"';
  print(specialCharacters); // Output: This string contains special characters: \n, ', and "
  
  String rawString = r'This is a raw string with no special characters: \n, \t, \f, \v, and \"';
  print(rawString); // Output: This is a raw string with no special characters: \n, \', and "
}

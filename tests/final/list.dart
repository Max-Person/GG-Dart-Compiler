void main() {
  // Create a list of integers
  var numbers = [1, 2, 3, 4, 5];

  // Test the length property
  print(numbers.length); // Output: 5
  
  // Test the [] operator for accessing elements
  print(numbers[0]); // Output: 1
  print(numbers[1]); // Output: 2
  
  // Test the add() method
  numbers.add(6);
  print(numbers); // Output: [1, 2, 3, 4, 5, 6]
  
  // Test the insert() method
  numbers.insert(0, 0);
  print(numbers); // Output: [0, 1, 2, 3, 4, 5, 6]
  
  // Test the remove() method
  numbers.remove(1);
  print(numbers); // Output: [0, 2, 3, 4, 5, 6]
  
  // Test reassignment to an empty list
  numbers = [];
  print(numbers); // Output: []
}
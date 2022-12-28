void main() {
  // Create a list of integers
  var numbers = [5, 2, 7, 1, 3];
  print(numbers);

  // Sort the list using the bubble sort algorithm
  for (int i = 0; i < numbers.length; i++) {
    for (int j = 0; j < numbers.length - i - 1; j++) {
      if (numbers[j] > numbers[j + 1]) {
        // Swap the elements if they are in the wrong order
        int temp = numbers[j];
        numbers[j] = numbers[j + 1];
        numbers[j + 1] = temp;
      }
    }
  }

  // Print the sorted list
  print(numbers); // Output: [1, 2, 3, 5, 7]
}

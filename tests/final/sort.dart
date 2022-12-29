void main() {
  // Create a list of integers
  print("Input array size:");
  int size = readInt();
  print("Input $size array elements (ints):");
  List<int> numbers = [];
  for(int i = 0; i < size; i ++){
    numbers.add(readInt());
  }

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
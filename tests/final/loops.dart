void main() {
  // Testing for loop
  print('Testing for loop:');
  for (int i = 1; i <= 5; i++) {
    print(i);
  }
  
  // Testing while loop
  print('\nTesting while loop:');
  int j = 1;
  while (j <= 5) {
    print(j);
    j++;
  }
  
  // Testing do-while loop
  print('\nTesting do-while loop:');
  int k = 1;
  do {
    print(k);
    k++;
  } while (k <= 5);
  
  // Testing break statement
  print('\nTesting break statement:');
  for (int i = 1; i <= 5; i++) {
    if (i == 3) {
      break;
    }
    print(i);
  }
  
  // Testing continue statement
  print('\nTesting continue statement:');
  for (int i = 1; i <= 5; i++) {
    if (i == 2 || i == 4) {
      continue;
    }
    print(i);
  }
}

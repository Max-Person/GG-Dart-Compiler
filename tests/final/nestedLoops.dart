void main() {
  // Testing nested loops with break and continue statements
  print('Testing nested loops with break and continue statements:');
  for (int i = 1; i <= 3; i++) {
    for (int j = 1; j <= 3; j++) {
      if (i == 1 && j == 2) {
        break;
      }
      if (i == 2 && j == 2) {
        continue;
      }
      print('i: $i, j: $j');
    }
  }
}
